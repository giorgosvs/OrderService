package es.merkle.component.model.api;

import es.merkle.component.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class SubmitOrderResponse {
    private Order order;
    private String message;
}
