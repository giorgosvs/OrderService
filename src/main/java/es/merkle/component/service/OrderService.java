package es.merkle.component.service;

import es.merkle.component.model.OrderType;
import es.merkle.component.model.Product;
import es.merkle.component.model.api.ModifyOrderRequest;
import es.merkle.component.repository.OrderRepository;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.entity.DbProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import es.merkle.component.mapper.OrderMapper;
import es.merkle.component.model.Order;
import es.merkle.component.model.OrderStatus;
import es.merkle.component.model.api.CreateOrderRequest;
import es.merkle.component.model.api.SubmitOrderRequest;
import es.merkle.component.model.api.SubmitOrderResponse;
import es.merkle.component.populating.PopulatorRunner;
import es.merkle.component.repository.adapter.OrderAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

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



    public Order modifyOrder(ModifyOrderRequest orderRequest) {
//        Retrieve a saved order by its ID
            Order order = orderMapper.mapModifyOrderRequestToOrder(orderRequest);
//        Decorate the order with processingProductId - Check if product exists todo handle exception here in a better way

        order.setOrderType(OrderType.ADD); //todo handle exception here in a better way
        Optional<DbProduct> product = Optional.ofNullable(productRepository.findById(orderRequest.getProductId()).orElseThrow(() -> new RuntimeException("Product not found")));
        order.setProcessingProductId(orderRequest.getProductId());

//        Decorate the order with orderType
//        Process the order.
//        Validate the order
//        Persist the updated order in the database.

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


}
