package es.merkle.component.mapper.dto;

import es.merkle.component.model.Order;
import es.merkle.component.model.api.SubmitOrderRequest;
import es.merkle.component.model.api.SubmitOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SubmitOrderMapper {

    @Mapping(source = "orderId", target = "id")
    void updateSubmitOrderRequestToOrder(SubmitOrderRequest submitOrderRequest, @MappingTarget Order order);

}
