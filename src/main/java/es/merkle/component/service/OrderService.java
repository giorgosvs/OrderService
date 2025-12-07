package es.merkle.component.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.merkle.component.mapper.ProductMapper;
import es.merkle.component.model.*;
import es.merkle.component.model.api.ModifyOrderRequest;
import es.merkle.component.repository.CustomerRepository;
import es.merkle.component.repository.OrderRepository;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.adapter.CustomerAdapter;
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
import java.util.stream.Collectors;

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
    private ProductMapper productMapper;
    @Autowired
    private OrderAdapter orderAdapter;
    @Autowired
    private CustomerAdapter customerAdapter;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;


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

        //Retrieve a saved order by its ID
        DbOrder savedOrder = orderRepository.findById(orderRequest.getOrderId()).orElseThrow(() -> new RuntimeException("Order not found"));

        //Check if requested product is available - todo handle exception here in a better way
        DbProduct reqProduct = productRepository.findById(orderRequest.getProductId()).orElseThrow(() -> new RuntimeException("Product not found"));

        //Check if customer exists
        Customer customer = customerAdapter.getCustomer(savedOrder.getCustomerId());

        Order order;

        //Check OrderType
        if(orderRequest.getOrderType() == OrderType.ADD) {

                savedOrder.getAddingProducts().add(orderRequest.getProductId());

                //Decorate the order with orderType
                order = orderMapper.mapModifyOrderRequestToOrder(orderRequest);

                order.setCustomerId(savedOrder.getCustomerId());


            } else if (orderRequest.getOrderType() == OrderType.REMOVE) {
                //safety check - product cannot be removed if not addded or product list is empty
                if( !savedOrder.getAddingProducts().isEmpty() && savedOrder.getAddingProducts().contains(orderRequest.getProductId())){

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
                throw new IllegalArgumentException("Invalid order type " + orderRequest.getOrderType());
            }



        //Process order here after product addition/removal

        List<Product> activeProducts = orderMapper.mapIdsToProducts(savedOrder.getAddingProducts());
        order.setAddingProducts(activeProducts);
        order.setRemoveProducts(orderMapper.mapIdsToProducts(savedOrder.getRemoveProducts()));


        //Calculate final price - no need to handle remove case, since final price is the sum of adding products
        BigDecimal finalPrice = BigDecimal.ZERO;
        for(String addingProductId : savedOrder.getAddingProducts()) {
            BigDecimal price = productRepository.findById(addingProductId).get().getPrice();
            finalPrice = finalPrice.add(price);
        }

        order.setFinalPrice(finalPrice);

        //Validate the order
        String orderStatus = validateOrder(savedOrder, activeProducts);

//        if(finalPrice.equals(BigDecimal.ZERO) || savedOrder.getAddingProducts().isEmpty()) { //means all products are removed and list is empty - return to initial order state NEW
//            savedOrder.setStatus(OrderStatus.NEW);
//        } else {
//            savedOrder.setStatus(OrderStatus.valueOf(orderStatus)); //proceed normally
//        }

        //set order status to both dbOrder and order
        savedOrder.setStatus(OrderStatus.valueOf(orderStatus));
        order.setStatus(OrderStatus.valueOf(orderStatus));
        order.setProcessingProductId(orderRequest.getProductId());

        //Set customer to order
        order.setCustomer(customer);

        System.out.println("DATABASE ORDER");
        System.out.println(savedOrder.toString());
        System.out.println("ORDER");
        System.out.println(order.toString());
        System.out.println("CUSTOMER");
        System.out.println(customer.toString());

        //Persist the updated order in the database.

        orderAdapter.saveOrder(order);
//        orderRepository.save(savedOrder);

        return order;
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
    private String validateOrder(DbOrder order, List<Product> activeProducts) {
//        The product status is NOT_AVAILABLE
//        The product expiry date is in the past
//        The product release date is in the future

        System.out.println("Date of order modification : " + LocalDate.now());
        //Convert product string status to enum and compare
//        String res = productMapper.mapStatus(product.getProductStatus()) == ProductStatus.NOT_AVAILABLE || product.getExpiringDate().isBefore(LocalDate.now()) || product.getReleasedDate().isAfter(LocalDate.now()) ? "INVALID" : "VALID";

        //Check for empty list, if so set state to 'NEW'
        if(order.getAddingProducts().isEmpty()) { //Initial state
            return "NEW";
        }

        boolean hasInvalidAddingProduct = activeProducts.stream()
                .anyMatch(p -> productMapper
                .mapStatus(String.valueOf(p.getProductStatus())) == ProductStatus.NOT_AVAILABLE
                || p.getExpiringDate().isBefore(LocalDate.now())
                || p.getReleasedDate().isAfter(LocalDate.now()));


        return hasInvalidAddingProduct ? "INVALID" : "VALID";
    }

}
