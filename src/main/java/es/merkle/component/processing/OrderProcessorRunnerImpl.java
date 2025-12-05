package es.merkle.component.processing;

import java.util.List;
import es.merkle.component.model.Order;
import es.merkle.component.processing.processor.OrderProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProcessorRunnerImpl implements OrderProcessorRunner {

    private final List<OrderProcessor> orderProcessors;

    @Override
    public void run(Order order) {
        for (int i = orderProcessors.size() - 1; i >= 0; i--) {
            if (orderProcessors.get(i).accepts(order)) {
                orderProcessors.get(i).process(order);
            }
        }
    }
}
