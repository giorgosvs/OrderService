package es.merkle.component.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import es.merkle.component.model.OrderItem;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.entity.DbOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import es.merkle.component.model.Order;
import es.merkle.component.model.Product;
import es.merkle.component.model.api.SubmitOrderRequest;
import es.merkle.component.repository.entity.DbOrder;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", imports = {UUID.class},uses = OrderItemMapper.class)
public abstract class OrderMapper {

    @Autowired
    protected OrderItemMapper orderItemMapper;

    public Order mapToOrder(DbOrder entity) {
        return Order.builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .finalPrice(entity.getFinalPrice())
                .submittedAt(entity.getSubmittedAt())
                .orderItems(
                        entity.getItems().stream()
                                .map(orderItemMapper::mapToOrderItem)
                                .collect(Collectors.toCollection(ArrayList::new))
                )
                .build();
    }

    public void mapToDbOrder(Order order, DbOrder dbOrder) {
        dbOrder.setStatus(order.getStatus());
        dbOrder.setFinalPrice(order.getFinalPrice());

        for (OrderItem item : order.getOrderItems()) {

            DbOrderItem existing = dbOrder.getItems().stream()
                    .filter(i -> i.getProduct().getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElse(null);

            if (existing == null) {
                DbOrderItem dbItem = orderItemMapper.mapToDbOrderItem(item);
                dbItem.setOrder(dbOrder);
                dbOrder.getItems().add(dbItem);
            } else {
                // ✅ update in place (NO new row)
                existing.setQuantity(item.getQuantity());
                existing.setUnitPrice(item.getUnitPrice());
            }
        }

        // Handle removals (optional but recommended)
        dbOrder.getItems().removeIf(dbItem ->
                order.getOrderItems().stream()
                        .noneMatch(oi -> oi.getProduct().getId().equals(dbItem.getProduct().getId()))
        );
    }

    @Mapping(source = "orderId", target = "id")
    public abstract void updateSubmitOrderRequestToOrder(SubmitOrderRequest submitOrderRequest, @MappingTarget Order order);


}
