package es.merkle.component.model.api;

import es.merkle.component.model.Order;
import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class SubmitOrderResponse {
    private Order order;
    private String message;
}
