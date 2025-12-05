package es.merkle.component.repository.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import es.merkle.component.mapper.OrderMapper;
import es.merkle.component.model.Order;
import es.merkle.component.repository.OrderRepository;
import es.merkle.component.repository.entity.DbOrder;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderAdapter {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderMapper orderMapper;

    public void saveOrder(Order order) {
        DbOrder dbOrder = orderMapper.mapToDbOrder(order);
        System.out.println("Saving Order Object : " + dbOrder.getId());
        orderRepository.save(dbOrder);
    }
}
