package es.merkle.component.model.api;

import es.merkle.component.model.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ModifyOrderRequest {
    private String orderId;
    private OrderType orderType;
    private String productId;
}
