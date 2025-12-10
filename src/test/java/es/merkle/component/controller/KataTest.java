package es.merkle.component.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import es.merkle.component.mapper.OrderMapper;
import es.merkle.component.mapper.ProductMapper;
import es.merkle.component.model.*;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.adapter.ProductAdapter;
import es.merkle.component.repository.entity.DbProduct;
import es.merkle.component.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.merkle.component.model.api.CreateOrderRequest;
import es.merkle.component.model.api.ModifyOrderRequest;
import es.merkle.component.model.api.SubmitOrderRequest;
import es.merkle.component.repository.CustomerRepository;
import es.merkle.component.repository.entity.DbCustomer;

@SpringBootTest
@AutoConfigureMockMvc
public class KataTest {

    public static final String EXPECTED_NAME = "Joel";
    public static final String EXPECTED_ADDRESS = "Av Camp Nou, 199";
    public static final String EXPECTED_PHONE_NUMBER = "839-758-3859";

    public static final String EXPECTED_VALID_ORDER_STATUS = "VALID";
    public static final String EXPECTED_INVALID_ORDER_STATUS = "INVALID";

    public static final String EXPECTED_AVAILABLE_PRODUCT_STATUS = "AVAILABLE";
    public static final String EXPECTED_SUBMITTED_ORDER_STATUS = "SUBMITTED";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;

    private DbCustomer dbCustomer;

    @BeforeEach
    public void setup() {
        dbCustomer = customerRepository.findAll().iterator().next();
    }

    //test create/add/submit
    @Test
    public void testValidOrderJourney() throws Exception {
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.id").value(dbCustomerId))
                .andExpect(jsonPath("$.customer.name").value(EXPECTED_NAME))
                .andExpect(jsonPath("$.customer.address").value(EXPECTED_ADDRESS))
                .andExpect(jsonPath("$.customer.phoneNumber").value(EXPECTED_PHONE_NUMBER))
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.ADD)
                .productId("NITFLIX")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        // Send modify request order to add NITFLIX for the last order
        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.id").value(dbCustomerId))
                .andExpect(jsonPath("$.customer.name").value(EXPECTED_NAME))
                .andExpect(jsonPath("$.customer.address").value(EXPECTED_ADDRESS))
                .andExpect(jsonPath("$.customer.phoneNumber").value(EXPECTED_PHONE_NUMBER))
                .andExpect(jsonPath("$.status").value(EXPECTED_VALID_ORDER_STATUS))
                .andExpect(jsonPath("$.finalPrice").value("12.99"))
                .andExpect(jsonPath("$.addingProducts[?(@.id=='NITFLIX')].productStatus").value(EXPECTED_AVAILABLE_PRODUCT_STATUS));

        SubmitOrderRequest submitOrderRequest = SubmitOrderRequest.builder()
                .orderId(orderId.get())
                .build();

        jsonRequest = objectMapper.writeValueAsString(submitOrderRequest);

        // Send modify request order to add NITFLIX for the last order
        ResultActions submitResultActions = performPost("/order-service/submit", jsonRequest)
                .andExpect(jsonPath("$.order.customer.id").value(dbCustomerId))
                .andExpect(jsonPath("$.order.customer.name").value(EXPECTED_NAME))
                .andExpect(jsonPath("$.order.customer.address").value(EXPECTED_ADDRESS))
                .andExpect(jsonPath("$.order.customer.phoneNumber").value(EXPECTED_PHONE_NUMBER))
                .andExpect(jsonPath("$.order.finalPrice").value("12.99"))
                .andExpect(jsonPath("$.order.addingProducts[?(@.id=='NITFLIX')].productStatus").value(EXPECTED_AVAILABLE_PRODUCT_STATUS))
                .andExpect(jsonPath("$.order.status").value(EXPECTED_SUBMITTED_ORDER_STATUS));
    }

    //test create-add-remove-submit
    @Test
    public void testAddRemoveValidProduct() throws Exception {
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.ADD)
                .productId("NITFLIX")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        // Send modify request order to add NITFLIX TWICE for the last order
        for(int i=0; i<2; i++) {
            performPost("/order-service/modify", jsonRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.addingProducts[?(@.id=='NITFLIX')]").isNotEmpty())
                    .andExpect(jsonPath("$.orderType").value(OrderType.ADD.name()));
        }
        //switch modification request to remove
        modifyOrderRequest.setOrderType(OrderType.REMOVE);

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        // Send modify request order to remove NITFLIX for the last order
        performPost("/order-service/modify", jsonRequest)
                .andExpect(jsonPath("$.finalPrice").value("12.99"))
                .andExpect(jsonPath("$.orderType").value(OrderType.REMOVE.name()))
                .andExpect(jsonPath("$.removeProducts[?(@.id=='NITFLIX')].id").value("NITFLIX"))
                .andExpect(jsonPath("$.status").value(EXPECTED_VALID_ORDER_STATUS));

        //test submission
        ResultActions submitResultActions = performPost("/order-service/submit", jsonRequest)
                .andExpect(jsonPath("$.order.finalPrice").value("12.99"))
                .andExpect(jsonPath("$.order.removeProducts[?(@.id=='NITFLIX')]").isNotEmpty())
                .andExpect(jsonPath("$.order.addingProducts[?(@.id=='NITFLIX')].productStatus").value(EXPECTED_AVAILABLE_PRODUCT_STATUS))
                .andExpect(jsonPath("$.order.status").value(EXPECTED_SUBMITTED_ORDER_STATUS))
                .andExpect(jsonPath("$.message").value("The order was submitted successfully"));


    }

    //testSubmitInvalidProductInOrder
    @Test
    public void testSubmitInvalidProduct() throws Exception{
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.ADD)
                .productId("SPITIFY")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        // Send modify request order to add invalid product SPITIFY for the last order
        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderType").value(OrderType.ADD.name()))
                .andExpect(jsonPath("$.addingProducts[?(@.id=='SPITIFY')]").isNotEmpty())
                .andExpect(jsonPath("$.status").value(EXPECTED_INVALID_ORDER_STATUS));

        SubmitOrderRequest submitOrderRequest = SubmitOrderRequest.builder()
                .orderId(orderId.get())
                .build();

        jsonRequest = objectMapper.writeValueAsString(submitOrderRequest);

        //test submission
        ResultActions submitResultActions = performPost("/order-service/submit", jsonRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("The order was not submitted because it's INVALID"));

    }

    //testPriceCalculation
    @Test
    public void testPriceCalculation() throws Exception {
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.ADD)
                .productId("NITFLIX")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        // Send modify request order to add and remove NITFLIX and DISNY products
        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalPrice").value("12.99"));
        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalPrice").value("25.98"));

        modifyOrderRequest.setProductId("DISNY");
        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalPrice").value("28.97"));

        modifyOrderRequest.setOrderType(OrderType.REMOVE);
        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalPrice").value("25.98"));

    }

    //testSubmitEmptyAddingProductList
    @Test
    public void testSubmitEmptyAddingProductList() throws Exception {
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        SubmitOrderRequest submitOrderRequest = SubmitOrderRequest.builder()
                .orderId(orderId.get())
                .build();

        jsonRequest = objectMapper.writeValueAsString(submitOrderRequest);

        ResultActions submitResultActions = performPost("/order-service/submit", jsonRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value( "The order was not submitted because it's not in a final status"));
    }

    //testSubmitAlreadySubmittedOrder
    @Test
    public void testSubmitAlreadySubmittedOrder() throws Exception {
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.ADD)
                .productId("NITFLIX")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        // Send modify request order to add and remove NITFLIX
        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk());

        SubmitOrderRequest submitOrderRequest = SubmitOrderRequest.builder()
                .orderId(orderId.get())
                .build();

        jsonRequest = objectMapper.writeValueAsString(submitOrderRequest);

        performPost("/order-service/submit", jsonRequest)
                .andExpect(status().isOk());

        ResultActions submitResultActions = performPost("/order-service/submit", jsonRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value( "Could not perform submission with order status being : SUBMITTED"));

    }

    @Test
    public void testRemoveProductFromEmptyAddingList() throws Exception {
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.REMOVE)
                .productId("NITFLIX")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        // Send modify request order to add and remove NITFLIX
        performPost("/order-service/modify", jsonRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value( "Cannot remove product: Order list is empty or does not contain requested product."));


    }

    @Test
    public void testRemoveNeverAddedProduct() throws Exception {
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.ADD)
                .productId("NITFLIX")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk());

        modifyOrderRequest.setOrderType(OrderType.REMOVE);
        modifyOrderRequest.setProductId("SPITIFY");
        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);


        // Send modify request order to remove never added SPITIFY
        performPost("/order-service/modify", jsonRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value( "Cannot remove product: Order list is empty or does not contain requested product."));


    }

    @Test
    public void testUnsupportedOrderType() throws Exception{
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        //invalid json for testing
        String invalidJson = """
      {
        "orderId": "%s",
        "orderType": "INVALID_TYPE",
        "productId": "NITFLIX"
      }
    """.formatted(orderId.get());

        performPost("/order-service/modify", invalidJson)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value( "JSON parse error"));

    }

    @Test
    public void modifyAlreadySubmittedOrder() throws Exception {
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        // Send the create order request for a random customer
        performPost("/order-service/create", jsonRequest)
                .andExpect(status().isOk())
                .andDo(result -> {
                    String order = result.getResponse().getContentAsString();
                    Order orderResponse = objectMapper.readValue(order, Order.class);
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.ADD)
                .productId("NITFLIX")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        // Send modify request order to add NITFLIX
        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk());

        SubmitOrderRequest submitOrderRequest = SubmitOrderRequest.builder()
                .orderId(orderId.get())
                .build();

        jsonRequest = objectMapper.writeValueAsString(submitOrderRequest);

        performPost("/order-service/submit", jsonRequest)
                .andExpect(status().isOk());

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        ResultActions submitResultActions = performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value( "Order is already submitted"));

    }

    @Test
    public void testExpiredProductValidity() throws Exception {
        saveExpiredProduct();
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        performPost("/order-service/create", jsonRequest)
                .andDo(result -> {
                    Order orderResponse = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            Order.class
                    );
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.ADD)
                .productId("EXP_PRODUCT")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EXPECTED_INVALID_ORDER_STATUS));
    }

    @Test
    public void testUnreleasedProductValidity() throws Exception {
        saveUnreleasedProduct();
        String dbCustomerId = dbCustomer.getId();
        AtomicReference<String> orderId = new AtomicReference<>("");
        String jsonRequest;

        CreateOrderRequest createOrderRequest = CreateOrderRequest.builder()
                .customerId(dbCustomerId)
                .build();

        jsonRequest = objectMapper.writeValueAsString(createOrderRequest);

        performPost("/order-service/create", jsonRequest)
                .andDo(result -> {
                    Order orderResponse = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            Order.class
                    );
                    orderId.set(orderResponse.getId());
                });

        ModifyOrderRequest modifyOrderRequest = ModifyOrderRequest.builder()
                .orderId(orderId.get())
                .orderType(OrderType.ADD)
                .productId("UN_PRODUCT")
                .build();

        jsonRequest = objectMapper.writeValueAsString(modifyOrderRequest);

        performPost("/order-service/modify", jsonRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(EXPECTED_INVALID_ORDER_STATUS));
    }

    //creates an expired product and saves it to db
    private void saveExpiredProduct() {
        LocalDate expdate = LocalDate.now().minusDays(10);
        LocalDate rdate = LocalDate.now().minusDays(100);

        DbProduct expiredProduct = new DbProduct();
        expiredProduct.setId("EXP_PRODUCT");
        expiredProduct.setName("Expired Product");
        expiredProduct.setPrice(new BigDecimal("9.99"));
        expiredProduct.setReleasedDate(rdate);
        expiredProduct.setExpiringDate(expdate);
        expiredProduct.setProductStatus("AVAILABLE");

        productRepository.save(expiredProduct);

    }

    //creates an unreleased product and saves it to db
    private void saveUnreleasedProduct() {
        LocalDate expdate = LocalDate.now().plusDays(100);
        LocalDate rdate = LocalDate.now().plusDays(1);

        DbProduct unreleasedProduct = new DbProduct();
        unreleasedProduct.setId("UN_PRODUCT");
        unreleasedProduct.setName("Expired Product");
        unreleasedProduct.setPrice(new BigDecimal("9.99"));
        unreleasedProduct.setReleasedDate(rdate);
        unreleasedProduct.setExpiringDate(expdate);
        unreleasedProduct.setProductStatus("AVAILABLE");

        productRepository.save(unreleasedProduct);

    }

    private ResultActions performPost(String uri, String jsonRequest) throws Exception {
        return mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));
    }

}