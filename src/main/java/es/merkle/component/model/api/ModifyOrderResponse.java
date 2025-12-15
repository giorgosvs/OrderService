package es.merkle.component.model.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.merkle.component.model.OrderItem;
import es.merkle.component.model.OrderStatus;
import es.merkle.component.model.OrderType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ModifyOrderResponse {
    private String id;
    private OrderType orderType;
    private OrderStatus status;
    private BigDecimal finalPrice;
    private List<OrderItem> orderItems = new ArrayList<>();
    private LocalDate submittedAt;
}
