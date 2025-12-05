package es.merkle.component.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Order {
    private String id;
    private String customerId;
    @JsonIgnore
    private String processingProductId;
    private OrderType orderType;
    private OrderStatus status;
    private List<Product> addingProducts = new ArrayList<>();
    private List<Product> removeProducts = new ArrayList<>();
    private BigDecimal finalPrice;
    private Customer customer;
}
