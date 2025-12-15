package es.merkle.component.mapper.dto;

import es.merkle.component.model.Order;
import es.merkle.component.model.api.CreateOrderRequest;
import es.merkle.component.model.api.CreateOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = java.util.UUID.class)
public interface CreateOrderMapper {

    @Mapping(target = "id", expression = "java(UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "orderItems", ignore = true)
    Order mapCreateOrderRequestToOrder(CreateOrderRequest request);

    CreateOrderResponse mapOrderToCreateOrderResponse(Order order);
}