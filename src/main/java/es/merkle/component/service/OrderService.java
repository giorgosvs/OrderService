package es.merkle.component.service;

import es.merkle.component.exception.InvalidOrderException;
import es.merkle.component.mapper.CustomerMapper;
import es.merkle.component.mapper.ProductMapper;
import es.merkle.component.mapper.dto.CreateOrderMapper;
import es.merkle.component.model.*;
import es.merkle.component.model.api.ModifyOrderRequest;
import es.merkle.component.repository.CustomerRepository;
import es.merkle.component.repository.OrderRepository;
import es.merkle.component.repository.adapter.CustomerAdapter;
import es.merkle.component.repository.adapter.ProductAdapter;
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
import org.springframework.transaction.annotation.Transactional;
import es.merkle.component.model.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private PopulatorRunner populatorRunner;
    @Autowired
    private CreateOrderMapper createOrderMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private OrderAdapter orderAdapter;
    @Autowired
    private CustomerAdapter customerAdapter;
    @Autowired
    private ProductAdapter productAdapter;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;

    //Create a new order with status 'NEW'
    @Transactional
    public Order createOrder(CreateOrderRequest orderRequest) {
        Order order = mapCreateOrderRequest(orderRequest);

        order.setStatus(OrderStatus.NEW);

        DbOrder dbOrder = new DbOrder();
        dbOrder.setId(order.getId());

        orderMapper.mapToDbOrder(order, dbOrder);
        //removed try-catch here, returned success message on failure
//        populateOrder(order); //populate order with customer information(CustomerOrderPopulator)
        //we don't need customer information


        orderRepository.save(dbOrder);
        return order;
    }

    @Transactional
    public Order modifyOrder(ModifyOrderRequest orderRequest) throws RuntimeException {
        //Retrieve a saved order by its ID todo test (Ex)
        DbOrder savedOrder = orderAdapter.getReqOrderById(orderRequest.getOrderId());
        Order order = orderMapper.mapToOrder(savedOrder);

        //Check if requested product is available todo test (Ex)
        DbProduct reqProduct = productAdapter.getReqProductById(orderRequest.getProductId());
        Product product = productMapper.mapToProduct(reqProduct);

        switch (orderRequest.getOrderType()) {
            case ADD -> order.addItem(product, orderRequest.getQuantity());

            case REMOVE -> order.removeItem(product, orderRequest.getQuantity());

            default -> throw new InvalidOrderException("Unsupported Order Type :" + orderRequest.getOrderType());
        }
        //Process order

        //Set the final price
        order.recalculateFinalPrice();
        //Validate the order
        order.setStatus(validateOrder(order));
        order.setSubmittedAt(LocalDateTime.now());

        //Persist the updated order in the database.
        saveOrder(order,savedOrder);

        return order;
    }

    @Transactional
    public SubmitOrderResponse submitOrder(SubmitOrderRequest submitOrderRequest) {

        //Retrieve saved order
        DbOrder savedOrder = orderAdapter.getReqOrderById(submitOrderRequest.getOrderId());
        //Map to order obj
        Order order = orderMapper.mapToOrder(savedOrder);
        orderMapper.updateSubmitOrderRequestToOrder(submitOrderRequest,order);

//        DbCustomer customer = customerMapper.mapCustomerToDbCustomer(order.getCustomer());
        //get the active products
        List<OrderItem> addingProducts = order.getOrderItems();

        //Handle submission
//        SubmitOrderResponse response = handleSubmitOrder(order, customer, addingProducts);

        //Save order to db
        saveOrder(order,savedOrder);
//        return response;
        return null;
    }

    private void saveOrder(Order order, DbOrder dbOrder
    ) {
        orderAdapter.saveOrder(order,dbOrder);
    }

    private void populateOrder(Order order) {
        populatorRunner.run(order);
    }

    private Order mapCreateOrderRequest(CreateOrderRequest orderRequest) {
        return createOrderMapper.mapCreateOrderRequestToOrder(orderRequest);
    }

    private OrderStatus validateOrder(Order order) {

        //Check if order is already submitted
        if(order.getStatus() == OrderStatus.SUBMITTED) {
            throw new InvalidOrderException("Order is already submitted");
        }
        //Check for empty list, if so set state to 'NEW'
        if(order.getOrderItems().isEmpty()) { //Initial state
            return OrderStatus.NEW;
        }
        //Check conditions
        boolean hasInvalidAddingProduct = order.getOrderItems().stream()
                .anyMatch(i -> i.getProduct().getProductStatus() == ProductStatus.NOT_AVAILABLE
                ||  i.getProduct().getExpiringDate().isBefore(LocalDate.now())
                ||  i.getProduct().getReleasedDate().isAfter(LocalDate.now()));

        return hasInvalidAddingProduct ? OrderStatus.INVALID : OrderStatus.VALID;
    }

//    private SubmitOrderResponse handleSubmitOrder(Order order, DbCustomer dbCustomer, List<Product> activeProducts) {
//
//        SubmitOrderResponse response = new SubmitOrderResponse();
//        response.setOrder(order);
//
//        //switch between OrderStatus to set response message
//        switch (order.getStatus()) {
//
//            case INVALID -> {
//                response.getOrder().setStatus(OrderStatus.FAILED);
//                response.setMessage("The order was not submitted because it's INVALID");
//                throw new InvalidOrderException(response.getMessage());
//            }
//            case VALID -> {
//                for (Product product : activeProducts) {
//                    //Add owned product to response body
//                    order.getCustomer().getOwnedProducts().add(product);
//
//                    DbProduct dbProduct = productMapper.mapToDbProduct(product);
//                    //Save owned product to db
//                    dbCustomer.getOwnedProducts().add(dbProduct);
//                }
//                //Persist changes -- should use adapter here as well
//                customerRepository.save(dbCustomer);
//
//                response.getOrder().setStatus(OrderStatus.SUBMITTED);
//                response.setMessage("The order was submitted successfully");
//            }
//            case NEW -> {
//                response.getOrder().setStatus(OrderStatus.NEW);
//                response.setMessage("The order was not submitted because it's not in a final status");
//                throw new InvalidOrderException(response.getMessage());
//            }
//            default -> {
//                throw new InvalidOrderException("Could not perform submission with order status being : "+ order.getStatus());
//            }
//        }
//
//        return response;
//    }
}





