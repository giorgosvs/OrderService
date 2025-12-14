package es.merkle.component.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapstruct.Mapper;
import es.merkle.component.model.Customer;
import es.merkle.component.repository.entity.DbCustomer;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer mapToCustomer(DbCustomer customer);
    DbCustomer mapCustomerToDbCustomer(Customer customer);
}
