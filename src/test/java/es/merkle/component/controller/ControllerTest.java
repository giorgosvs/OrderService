package es.merkle.component.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.merkle.component.model.Order;
import es.merkle.component.model.OrderType;
import es.merkle.component.model.api.CreateOrderRequest;
import es.merkle.component.model.api.ModifyOrderRequest;
import es.merkle.component.model.api.SubmitOrderRequest;
import es.merkle.component.repository.CustomerRepository;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.entity.DbCustomer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ControllerTest {

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


    @Test
    public void testValidOrderJourney() throws Exception {
        String customerId = dbCustomer.getId();
        String orderId = createOrder(customerId);

        modifyOrder(orderId, OrderType.ADD, "NITFLIX", 1)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.orderType").value("ADD"))
                .andExpect(jsonPath("$.status").value("VALID"))
                .andExpect(jsonPath("$.finalPrice").value(12.99))
                .andExpect(jsonPath("$.orderItems[0].product.id").value("NITFLIX"))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(1))
                .andExpect(jsonPath("$.orderItems[0].unitPrice").value(12.99));

        submitOrder(orderId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("The order was submitted successfully"));
    }

    @Test
    void addingSameProductIncrementsQuantity() throws Exception {
        String orderId = createOrder(dbCustomer.getId());

        modifyOrder(orderId, OrderType.ADD, "NITFLIX", 1);

        modifyOrder(orderId, OrderType.ADD, "NITFLIX", 1)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItems[0].product.id").value("NITFLIX"))
                .andExpect(jsonPath("$.orderItems[0].quantity").value(2))
                .andExpect(jsonPath("$.finalPrice").value(25.98));
    }

    @Test
    //Testing of product addition with negative quantity, it should fail
    void addProductWithNegativeQuantity() throws Exception {
        String orderId = createOrder(dbCustomer.getId());

        modifyOrder(orderId, OrderType.ADD, "NITFLIX", -1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Quantity must be greater than zero"));
    }

    @Test
    //Test of product addition with zero quantity, it should fail
    void addProductWithZeroQuantity() throws Exception {
        String orderId = createOrder(dbCustomer.getId());

        modifyOrder(orderId, OrderType.ADD, "NITFLIX", 0)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Quantity must be greater than zero"));
    }

    @Test
    //Tests that exception throws when product is not found in order items
    void testRemoveProductNotInOrderItems() throws Exception {
        String orderId = createOrder(dbCustomer.getId());

        modifyOrder(orderId, OrderType.REMOVE, "NITFLIX", 1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Product not found in order"));
    }

    @Test
    //Test when more of the existing products are being removed if the order item deletes
    void removeMoreThanExistingQuantity() throws Exception {
        String orderId = createOrder(dbCustomer.getId());

        modifyOrder(orderId, OrderType.ADD, "NITFLIX", 1);

        modifyOrder(orderId, OrderType.REMOVE, "NITFLIX", 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderItems").isEmpty())
                .andExpect(jsonPath("$.finalPrice").value(0));
    }

    @Test
    //Test submission of empty order in status NEW
    void submitEmptyOrder() throws Exception {
        String orderId = createOrder(dbCustomer.getId());

        submitOrder(orderId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("The order was not submitted because it's not in a final status"));
    }

    @Test
    //Test the submission of an invalid order
    void submitInvalidOrder() throws Exception {
        String orderId = createOrder(dbCustomer.getId());

        modifyOrder(orderId, OrderType.ADD, "SPITIFY", 1);

        submitOrder(orderId)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("The order was not submitted because it's INVALID"));
    }

    @Test
    //Test unsupported order type
    void unsupportedOrderType() throws Exception {
        String orderId = createOrder(dbCustomer.getId());

        String invalidJson = """
        {
          "orderId": "%s",
          "orderType": "INVALID_TYPE",
          "productId": "NITFLIX",
          "quantity": 1
        }
        """.formatted(orderId);

        performPost("/order-service/modify", invalidJson)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("JSON parse error"));
    }

    @Test
    //Test modification of already submitted order
    void modifySubmittedOrder() throws Exception {
        String orderId = createOrder(dbCustomer.getId());

        modifyOrder(orderId, OrderType.ADD, "NITFLIX", 1);
        submitOrder(orderId);

        modifyOrder(orderId, OrderType.ADD, "NITFLIX", 1)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Order is already submitted"));
    }


    //HELPER METHODS

    //Create a new order
    private String createOrder(String customerId) throws Exception {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .customerId(customerId)
                .build();

        ResultActions result = performPost(
                "/order-service/create",
                objectMapper.writeValueAsString(request)
        ).andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.status").value("NEW"));


        Order order = objectMapper.readValue(
                result.andReturn().getResponse().getContentAsString(),
                Order.class
        );
        return order.getId();
    }

    //Modify an order, returns
    private ResultActions modifyOrder(String orderId, OrderType type, String productId, int quantity) throws Exception {
        ModifyOrderRequest request = ModifyOrderRequest.builder()
                .orderId(orderId)
                .orderType(type)
                .productId(productId)
                .quantity(quantity)
                .build();

        return performPost(
                "/order-service/modify",
                objectMapper.writeValueAsString(request)
        );
    }

    private ResultActions submitOrder(String orderId) throws Exception {
        SubmitOrderRequest request = SubmitOrderRequest.builder()
                .orderId(orderId)
                .build();

        return performPost(
                "/order-service/submit",
                objectMapper.writeValueAsString(request)
        );
    }

    private ResultActions performPost(String uri, String jsonRequest) throws Exception {
        return mockMvc.perform(post(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest));
    }

}
