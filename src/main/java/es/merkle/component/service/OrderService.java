package es.merkle.component.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.merkle.component.model.*;
import es.merkle.component.model.api.ModifyOrderRequest;
import es.merkle.component.repository.CustomerRepository;
import es.merkle.component.repository.OrderRepository;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.entity.DbCustomer;
import es.merkle.component.repository.entity.DbOrder;
import es.merkle.component.repository.entity.DbProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.merkle.component.mapper.OrderMapper;
import es.merkle.component.model.api.CreateOrderRequest;
import es.merkle.component.model.api.SubmitOrderRequest;
import es.merkle.component.model.api.SubmitOrderResponse;
import es.merkle.component.populating.PopulatorRunner;
import es.merkle.component.repository.adapter.OrderAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);


    private static final String flagProductNotFound = "PRODUCT_NOT_FOUND";
    private static final String flagPriceIncorrect = "PRICES_INCORRECT";
    private static final String flagItemNotAvailable = "ITEM_NOT_AVAILABLE";
    private static final String flagItemExpire = "ITEM_EXPIRED";
    private static final String flagOrderDoesNotExist = "ORDER_DOES_NOT_EXIST";

    @Autowired
    private PopulatorRunner populatorRunner;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderAdapter orderAdapter;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Order order;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate date;

    //Creates a new order with orderStatus 'NEW' -> todo (?) How many orders is a customer allowed to create
    public Order createOrder(CreateOrderRequest orderRequest) {
        Order order = mapCreateOrderRequest(orderRequest);

//        System.out.println("OrderRequest to Order is : " + order.toString());

        try {
            populateOrder(order); //populates with customer information(CustomerOrderPopulator) to enrich order object
            saveOrder(order);
        } catch (Exception e) {
            log.error(e.getMessage());
//            log.error("Error populating order", e);

        }
        return order;
    }



    public Order modifyOrder(ModifyOrderRequest orderRequest) throws RuntimeException {

        System.out.println(":: Inside modify Order ::");
        //Retrieve a saved order by its ID
        DbOrder savedOrder = orderRepository.findById(orderRequest.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));
        //Check if requested product is available - todo handle exception here in a better way
        DbProduct reqProduct = productRepository.findById(orderRequest.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));

        if(savedOrder!=null) { //todo condition here, exception is thrown - maybe it is not needed

            //Check OrderType
            if(orderRequest.getOrderType() == OrderType.ADD) {

                savedOrder.getAddingProducts().add(orderRequest.getProductId());

                //Decorate the order with orderType
                order = orderMapper.mapModifyOrderRequestToOrder(orderRequest);

                order.setCustomerId(savedOrder.getCustomerId());

                System.out.println("DATABASE ORDER");
                System.out.println(savedOrder.toString());
                order.setProcessingProductId(orderRequest.getProductId());
                System.out.println("ORDER");
                System.out.println(order.toString());


            } else if (orderRequest.getOrderType() == OrderType.REMOVE) {
                //safety check - product cannot be removed if not addded or product list is empty
                if(!savedOrder.getAddingProducts().isEmpty() || !savedOrder.getAddingProducts().contains(orderRequest.getProductId())){

                    //Decorate the order with orderType
                    order = orderMapper.mapModifyOrderRequestToOrder(orderRequest);

                    //remove from AddingProducts
                    savedOrder.getAddingProducts().remove(orderRequest.getProductId());
                    //add to RemoveProducts
                    savedOrder.getRemoveProducts().add(orderRequest.getProductId());


                } else {
                    throw new RuntimeException("Cannot Remove: Order list is empty or does not contain requested product.");
                }

                //Decorate the order with orderType
                order = orderMapper.mapModifyOrderRequestToOrder(orderRequest);

                order.setCustomerId(savedOrder.getCustomerId());

            } else {
                throw new RuntimeException("Invalid order type");
            }
        }


        //Process order here after product addition/removal
        BigDecimal finalPrice = new BigDecimal("0.0");
        for(String addingProductId : savedOrder.getAddingProducts()) {
            BigDecimal price = new BigDecimal(String.valueOf(productRepository.findById(addingProductId).get().getPrice()));
            finalPrice.add(price);
        }
        //retrieve last object and pop the elements matching the removeProducts[]
        //Calculate final price on addition/removal of product - use the reqProduct.getPrice() to do so


        //Validate the order
        validateOrder(savedOrder, reqProduct);
        //Persist the updated order in the database.
        //Return enriched order object
        return null; //todo
    }

    public SubmitOrderResponse submitOrder(SubmitOrderRequest submitOrderRequest) {
        Order order = new Order(); // TODO: Idk how to do this
        SubmitOrderResponse submitOrderResponse = new SubmitOrderResponse();
        submitOrderResponse.setOrder(order);

        if (order.getStatus() == OrderStatus.INVALID) {
            submitOrderResponse.getOrder().setStatus(OrderStatus.FAILED);
            submitOrderResponse.setMessage("The order was not submitted because it's INVALID");
            return submitOrderResponse;
        } else if (order.getStatus() == OrderStatus.VALID) {
            submitOrderResponse.setMessage("The order was submitted successfully");
            submitOrderResponse.getOrder().setStatus(OrderStatus.SUBMITTED);
        } else if (order.getStatus() == OrderStatus.NEW) {
            submitOrderResponse.setMessage("The was not submitted because it's not in a final status");
            submitOrderResponse.getOrder().setStatus(OrderStatus.FAILED);
        } else {
            return null;
        }

        //Order class has atrribute of Customer, so going to use mapCustomer method for conversion
//                order.setCustomer(orderMapper.mapCustomer(savedOrder.getCustomer()));

        return submitOrderResponse;
    }

    private void saveOrder(Order order) {
        orderAdapter.saveOrder(order);
    }

    private void populateOrder(Order order) {
        populatorRunner.run(order);
    }

    private Order mapCreateOrderRequest(CreateOrderRequest orderRequest) {
        return orderMapper.mapCreateOrderRequestToOrder(orderRequest);
    }

    //should return a list of OrderResponse dto
    public List<SubmitOrderResponse> getAllOrders() {
        return null;
    }

    //Maybe use Logger here
    private String validateOrder(DbOrder order, DbProduct product) {
//        The product status is NOT_AVAILABLE
//        The product expiry date is in the past
//        The product release date is in the future
        System.out.println("Date of order modification : " + LocalDate.now());
        String res = product.getProductStatus() == "NOT AVAILABLE" || product.getExpiringDate().isBefore(date.now()) || product.getReleasedDate().isAfter(date.now()) ? "INVALID" : "VALID";

        return res;
    }

    //todo
    private Order processOrder() {
        return null;
    }

}
