package es.merkle.component.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.merkle.component.model.OrderItem;
import es.merkle.component.repository.entity.DbOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface OrderItemMapper {

    OrderItem mapToOrderItem(DbOrderItem dbOrderItem);

    @Mapping(target = "order", ignore = true)
    DbOrderItem mapToDbOrderItem(OrderItem OrderItem);
}
