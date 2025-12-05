package es.merkle.component.model.api;

import es.merkle.component.model.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class SubmitOrderRequest {
    @NotEmpty
    private String orderId;
}
