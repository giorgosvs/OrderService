package es.merkle.component.service;

import es.merkle.component.exception.InvalidOrderException;
import es.merkle.component.exception.ResourceNotFoundException;
import es.merkle.component.mapper.CustomerMapper;
import es.merkle.component.mapper.ProductMapper;
import es.merkle.component.mapper.dto.CreateOrderMapper;
import es.merkle.component.mapper.dto.ModifyOrderMapper;
import es.merkle.component.mapper.dto.SubmitOrderMapper;
import es.merkle.component.model.*;
import es.merkle.component.model.api.*;
import es.merkle.component.repository.CustomerRepository;
import es.merkle.component.repository.OrderRepository;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.adapter.CustomerAdapter;
import es.merkle.component.repository.adapter.ProductAdapter;
import es.merkle.component.repository.entity.DbCustomer;
import es.merkle.component.repository.entity.DbOrder;
import es.merkle.component.repository.entity.DbProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.merkle.component.mapper.OrderMapper;
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
    private ModifyOrderMapper modifyOrderMapper;
    @Autowired
    private SubmitOrderMapper submitOrderMapper;
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
    @Autowired
    private ProductRepository productRepository;

    //Create a new order with status 'NEW'
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest orderRequest) {
        //map CreateOrderRequest to order
        Order order = mapCreateOrderRequest(orderRequest);

        //Fetch customer data
        DbCustomer customer = customerRepository.findById(orderRequest.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        //create new db order and set the new UUID and customer
        DbOrder dbOrder = new DbOrder();
        dbOrder.setId(order.getId());
        dbOrder.setCustomer(customer);

        //map order to entity
        orderMapper.mapToDbOrder(order, dbOrder);
        //map order to response
        CreateOrderResponse response = createOrderMapper.mapOrderToCreateOrderResponse(order);
        //removed try-catch here, returned success message on failure
//        populateOrder(order); //populate order with customer information(CustomerOrderPopulator)

        //save entity
        orderRepository.save(dbOrder);
        return response;
    }

    @Transactional
    public ModifyOrderResponse modifyOrder(ModifyOrderRequest orderRequest) throws RuntimeException {
        //Retrieve a saved order by its ID
        DbOrder savedOrder = orderAdapter.getReqOrderById(orderRequest.getOrderId());

        //Check if requested product is available
        DbProduct reqProduct = productAdapter.getReqProductById(orderRequest.getProductId());
        Product product = productMapper.mapToProduct(reqProduct);

        //map entity to order and map request to order
        Order order = orderMapper.mapToOrder(savedOrder);
        modifyOrderMapper.updateOrderFromRequest(orderRequest, order);

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
        //Add timestamp
        order.setSubmittedAt(LocalDate.now());
        //SetOrderType
        order.setOrderType(orderRequest.getOrderType());

        //Persist the updated order in the database.
        saveOrder(order,savedOrder);
        //Map response
        ModifyOrderResponse response = modifyOrderMapper.mapOrderToModifyOrderResponse(order);

        return response;
    }

    @Transactional
    public SubmitOrderResponse submitOrder(SubmitOrderRequest submitOrderRequest) {

        //Retrieve saved order
        DbOrder savedOrder = orderAdapter.getReqOrderById(submitOrderRequest.getOrderId());
        //Retrieve customer
        DbCustomer customer = savedOrder.getCustomer();
        if (customer == null) {
            throw new IllegalStateException("Order has no customer");
        }
        //Map to order obj
        Order order = orderMapper.mapToOrder(savedOrder);
        submitOrderMapper.updateSubmitOrderRequestToOrder(submitOrderRequest,order);

        //get the active products
        List<OrderItem> orderItems = order.getOrderItems();

        switch(order.getStatus()){
            case NEW -> throw new InvalidOrderException("The order was not submitted because it's not in a final status");
            case INVALID -> {
//                order.setStatus(OrderStatus.FAILED);
//                saveOrder(order,savedOrder);
                throw new InvalidOrderException("The order was not submitted because it's INVALID");
//                return new SubmitOrderResponse("The order was not submitted because it's INVALID");
            }
        }

        //Handle submission
        handleSubmitOrder(order, customer, orderItems);

        //Save order to db
        saveOrder(order,savedOrder);

        return new SubmitOrderResponse("The order was submitted successfully");
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

    private void handleSubmitOrder(Order order, DbCustomer dbCustomer, List<OrderItem> orderItems) {
                for (OrderItem orderItem : orderItems) {
                    //Add the owned products for the dbCustomer

                    //Fetch dbProduct and map to orderItem
                    DbProduct product = productRepository.findById(orderItem.getProduct().getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

                    //Save owned product to db
                    if(!dbCustomer.getOwnedProducts().contains(product)) {
                        dbCustomer.getOwnedProducts().add(product);
                    }
                }
                //Persist changes
                customerRepository.save(dbCustomer);

                order.setStatus(OrderStatus.SUBMITTED);
    }
}





