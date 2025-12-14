package es.merkle.component.mapper.dto;

import es.merkle.component.model.Order;
import es.merkle.component.model.api.ModifyOrderRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ModifyOrderMapper {

    @Mapping(source = "orderId", target = "id")
    void updateOrderFromRequest(ModifyOrderRequest request, @MappingTarget Order order);
}
