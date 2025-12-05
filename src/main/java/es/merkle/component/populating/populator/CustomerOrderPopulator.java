package es.merkle.component.populating.populator;

import org.springframework.stereotype.Component;
import es.merkle.component.model.Customer;
import es.merkle.component.model.Order;
import es.merkle.component.repository.adapter.CustomerAdapter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomerOrderPopulator implements OrderPopulator {

    private final CustomerAdapter customerAdapter;

    @Override
    public void populate(Order order) {
        Customer customer = getCustomer(order.getCustomerId());
        order.setCustomer(customer);
    }

    private Customer getCustomer(String id) {
        return customerAdapter.getCustomer(id);
    }
}
