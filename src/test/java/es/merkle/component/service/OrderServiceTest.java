package es.merkle.component.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.merkle.component.mapper.OrderMapper;
import es.merkle.component.mapper.dto.CreateOrderMapper;
import es.merkle.component.model.Order;
import es.merkle.component.model.OrderStatus;
import es.merkle.component.model.api.CreateOrderRequest;
import es.merkle.component.model.api.CreateOrderResponse;
import es.merkle.component.repository.CustomerRepository;
import es.merkle.component.repository.OrderRepository;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.entity.DbCustomer;
import es.merkle.component.repository.entity.DbOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CreateOrderMapper createOrderMapper;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

//
//    @Test
//    void createOrder_withExistingCustomer_shouldSucceed() {
//        CreateOrderRequest request = new CreateOrderRequest("cust-123");
//
//        DbCustomer customer = DbCustomer.builder()
//                .id("cust-123")
//                .build();
//
//        Order domainOrder = Order.builder()
//                .id("generated-uuid")
//                .status(OrderStatus.NEW)
//                .build();
//
//        DbOrder dbOrder = DbOrder.builder()
//                .id("generated-uuid")
//                .customer(customer)
//                .status(OrderStatus.NEW)
//                .build();
//
//        when(customerRepository.findById("cust-123"))
//                .thenReturn(Optional.of(customer));
//
//        when(createOrderMapper.mapCreateOrderRequestToOrder(request))
//                .thenReturn(domainOrder);
//
//        when(orderMapper.mapToDbOrder(domainOrder))
//                .thenReturn(dbOrder);
//
//        when(orderRepository.save(dbOrder))
//                .thenReturn(dbOrder);
//
//        when(orderPersistenceMapper.toOrder(dbOrder))
//                .thenReturn(domainOrder);
//
//        CreateOrderResponse response = orderService.createOrder(request);
//
//        assertThat(response.getId()).isEqualTo("generated-uuid");
//        assertThat(response.getStatus()).isEqualTo(OrderStatus.NEW);
//    }


}
