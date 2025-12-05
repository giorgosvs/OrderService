package es.merkle.component.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.merkle.component.model.Order;
import es.merkle.component.model.OrderType;
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
    public static final String EXPECTED_AVAILABLE_PRODUCT_STATUS = "AVAILABLE";
    public static final String EXPECTED_SUBMITTED_ORDER_STATUS = "SUBMITTED";
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    private DbCustomer dbCustomer;

    @BeforeEach
    public void setup() {
        dbCustomer = customerRepository.findAll().iterator().next();
    }

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

    private ResultActions performPost(String uri, String jsonRequest) throws Exception {
        return mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));
    }

}