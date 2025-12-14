package es.merkle.component.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import es.merkle.component.exception.InvalidOrderException;
import lombok.*;

@Builder
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@ToString
public class Order {
    private String id;
    private String customerId;
    private OrderType orderType;
    private OrderStatus status;
    private BigDecimal finalPrice;

    //attributes added
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();
    private LocalDateTime submittedAt;

    //OrderType - ADD
    public void addItem(Product product, Integer quantity) {
        OrderItem existing = findItem(product);

        if(existing != null) {
            existing.increaseQuantity(quantity); //OrderItem there so increase quantity
        }else {
            orderItems.add(OrderItem.create(product,quantity)); //OrderItem not there so create new
        }

    }

    //OrderType REMOVE
    public void removeItem(Product product, Integer quantity) {
        OrderItem existing = findItem(product);

        if(existing ==null) { //check if product is there
            throw new InvalidOrderException("Product not found in order");
        } else {
            existing.decreaseQuantity(quantity); //if it is there decrease quantity

            if (existing.getQuantity() <= 0) {
                orderItems.remove(existing); //if all products of this type are removed, then remove OrderItem
            }
        }
    }

    //function for recalculation of price after addition/removal
    public void recalculateFinalPrice() {
        this.finalPrice = orderItems.stream()
                .map(OrderItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private OrderItem findItem(Product product){
        if (product == null || product.getId() == null) {
            throw new InvalidOrderException("Product or productId is null");
        }

        return orderItems.stream()
                .filter(oi -> oi.getProduct() != null && oi.getProduct().getId() != null)
                .filter(oi -> product.getId().equals(oi.getProduct().getId()))
                .findFirst()//could be more so return first
                .orElse(null);//throw new exception here
    }
}
