package es.merkle.component.repository.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Table;
import es.merkle.component.model.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity(name = "orders")
@Table(name = "orders")
@Getter
@Setter
@ToString
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
public class DbOrder {

    @Id
    private String id;
//    private String customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ElementCollection
    private List<String> addingProducts = new ArrayList<>();
    @ElementCollection
    private List<String> removeProducts = new ArrayList<>();
    private BigDecimal finalPrice;

    @ManyToOne(fetch = FetchType.LAZY) //used JPA relationship here Many to One
    @JoinColumn(name = "customer_id") //joined object under customerId. Now it references to customer with foreign key relationship
    private DbCustomer customer; //Customer -> DbCustomer
}
