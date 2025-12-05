package es.merkle.component.repository.adapter;

import org.springframework.stereotype.Component;
import es.merkle.component.exception.CustomerNotFoundException;
import es.merkle.component.mapper.CustomerMapper;
import es.merkle.component.model.Customer;
import es.merkle.component.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomerAdapter {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public Customer getCustomer(String id) {
        return customerRepository.findById(id)
                .map(customerMapper::mapToCustomer)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }
}
