package es.merkle.component.mapper;

import org.mapstruct.Mapper;
import es.merkle.component.model.Customer;
import es.merkle.component.repository.entity.DbCustomer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    Customer mapToCustomer(DbCustomer customer);
}
