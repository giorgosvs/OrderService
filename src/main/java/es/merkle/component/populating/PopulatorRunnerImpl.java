package es.merkle.component.populating;

import java.util.List;
import org.springframework.stereotype.Component;
import es.merkle.component.model.Order;
import es.merkle.component.populating.populator.OrderPopulator;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PopulatorRunnerImpl implements PopulatorRunner {

    private final List<OrderPopulator> orderPopulators;

    @Override
    public void run(Order order) {
        for (int i = orderPopulators.size() - 1; i >= 0; i--) {
            orderPopulators.get(i).populate(order);
        }
    }
}
