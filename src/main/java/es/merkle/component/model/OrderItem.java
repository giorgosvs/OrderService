package es.merkle.component.model;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class OrderItem {
    private Product product;
    private Integer quantity;
    private BigDecimal unitPrice;

    public static OrderItem create(Product product, int quantity) {
        return OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .build();
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        this.quantity -= amount;
    }

    public BigDecimal totalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
