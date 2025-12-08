package es.merkle.component.model.api;

import es.merkle.component.model.NotEmpty;
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
    @NotEmpty
    private String orderId;
    @NotEmpty
    private OrderType orderType;
    @NotEmpty
    private String productId;
}
