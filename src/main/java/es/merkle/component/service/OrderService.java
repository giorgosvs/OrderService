package es.merkle.component.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.merkle.component.exception.InvalidOrderException;
import es.merkle.component.mapper.ProductMapper;
import es.merkle.component.model.*;
import es.merkle.component.model.api.ModifyOrderRequest;
import es.merkle.component.repository.adapter.CustomerAdapter;
import es.merkle.component.repository.adapter.ProductAdapter;
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
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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
    private ProductAdapter productAdapter;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate date;

    //Creates a new order with orderStatus 'NEW' -> todo (?) How many orders is a customer allowed to create
    public Order createOrder(CreateOrderRequest orderRequest) {
        Order order = mapCreateOrderRequest(orderRequest);
        //removed try-catch here, returned success message on failure
        populateOrder(order); //populates order with customer information(CustomerOrderPopulator)
        saveOrder(order);
        return order;
    }

    public Order modifyOrder(ModifyOrderRequest orderRequest) throws RuntimeException {

        //Retrieve a saved order by its ID
        DbOrder savedOrder = orderAdapter.getReqOrderById(orderRequest.getOrderId());

        //Check if requested product is available
        DbProduct reqProduct = productAdapter.getReqProductById(orderRequest.getProductId());

        //Retrieve customer
        Customer customer = customerAdapter.getCustomer(savedOrder.getCustomerId());

        Order order;

        //Check OrderType
        if(orderRequest.getOrderType() == OrderType.ADD) {

                savedOrder.getAddingProducts().add(orderRequest.getProductId());
                //Decorate the order with orderType
                order = orderMapper.mapModifyOrderRequestToOrder(orderRequest);

        } else if (orderRequest.getOrderType() == OrderType.REMOVE) {
                //safety check - product cannot be removed if not addded or product list is empty
                if( !savedOrder.getAddingProducts().isEmpty() && savedOrder.getAddingProducts().contains(orderRequest.getProductId())){
                    //remove from AddingProducts
                    savedOrder.getAddingProducts().remove(orderRequest.getProductId());
                    //add to RemoveProducts
                    savedOrder.getRemoveProducts().add(orderRequest.getProductId());

                } else {
                    throw new InvalidOrderException("Cannot remove product: Order list is empty or does not contain requested product.");
                }

                //Decorate the order with orderType
                order = orderMapper.mapModifyOrderRequestToOrder(orderRequest);

        } else {
            throw new InvalidOrderException("Unsupported order type " + orderRequest.getOrderType());
        }

        //Set the customer id
        order.setCustomerId(savedOrder.getCustomerId());

        //Process order
        //Save th adding product list(activeProducts) for order validation
        List<Product> activeProducts = orderMapper.mapIdsToProducts(savedOrder.getAddingProducts());

        order.setAddingProducts(activeProducts);
        order.setRemoveProducts(orderMapper.mapIdsToProducts(savedOrder.getRemoveProducts()));

        //Set the final price
        order.setFinalPrice(calulateFinalPrice(savedOrder.getAddingProducts()));

        //Validate the order
        String orderStatus = validateOrder(savedOrder, activeProducts);

        //set order status
        order.setStatus(OrderStatus.valueOf(orderStatus));
        order.setProcessingProductId(orderRequest.getProductId());

        //Set customer to order
        order.setCustomer(customer);

        //Persist the updated order in the database.
        saveOrder(order);

        return order;
    }

    public SubmitOrderResponse submitOrder(SubmitOrderRequest submitOrderRequest) {

        //Retrieve saved order
        DbOrder savedOrder = orderAdapter.getReqOrderById(submitOrderRequest.getOrderId());

        //Map to order obj
        Order order = orderMapper.mapToOrder(savedOrder);

        //Handle submission
        SubmitOrderResponse response = handleSubmitOrder(order);

        //Save order to db
        saveOrder(order);
        return response;
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

//    public List<SubmitOrderResponse> getAllOrders() {
//        return null;
//    }

    //Maybe use Logger here
    private String validateOrder(DbOrder order, List<Product> activeProducts) {

        //Check if order already submitted
        if(order.getStatus() == OrderStatus.SUBMITTED) {
            throw new InvalidOrderException("Order is already submitted");
        }
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

    //calculate final price
    private BigDecimal calulateFinalPrice(List<String> addingProdcuts) {
        //Calculate final price - no need to handle remove case, since final price is the sum of adding products
        BigDecimal finalPrice = BigDecimal.ZERO;
        //todo maybe add a check here for price
        for(String addingProductId : addingProdcuts) {
            BigDecimal price = productAdapter.getReqProductById(addingProductId).getPrice();
            finalPrice = finalPrice.add(price);
        }
        return finalPrice;
    }

    private SubmitOrderResponse handleSubmitOrder(Order order) {

        SubmitOrderResponse response = new SubmitOrderResponse();
        response.setOrder(order);

        //switch between OrderStatus to set appropriate response message
        switch (order.getStatus()) {

            case INVALID -> {
                response.getOrder().setStatus(OrderStatus.FAILED);
                response.setMessage("The order was not submitted because it's INVALID");
            }

            //use methods of mapper jackson
            case VALID -> {
                //Pass products into owned products List of products
                List<Product> addingProducts = order.getAddingProducts();
                for (Product product : addingProducts) {
                    order.getCustomer().getOwnedProducts().add(product);
                }
                response.getOrder().setStatus(OrderStatus.SUBMITTED);
                response.setMessage("The order was submitted successfully");
            }
            case NEW -> {
                response.getOrder().setStatus(OrderStatus.NEW);
                response.setMessage("The order was not submitted because it's not in a final status");
            }
            //if order status is different from these, throw invalid order exception
            default -> {
                throw new InvalidOrderException("Could not perform submission with status :  "+ order.getStatus());
            }
        }

        return response;
    }

}





