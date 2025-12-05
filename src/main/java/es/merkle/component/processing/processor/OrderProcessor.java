package es.merkle.component.processing.processor;

import es.merkle.component.model.Order;

public interface OrderProcessor {
    boolean accepts(Order order);
    void process(Order order);
}
