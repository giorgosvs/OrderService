package es.merkle.component.processing;

import es.merkle.component.model.Order;

public interface OrderProcessorRunner {
    void run(Order order);
}
