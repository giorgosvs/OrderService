package es.merkle.component.model.api;

import es.merkle.component.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class CreateOrderResponse {
    private String id;
    private OrderStatus status;

}
