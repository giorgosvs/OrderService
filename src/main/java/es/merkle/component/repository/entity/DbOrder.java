package es.merkle.component.repository.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Table;
import es.merkle.component.model.OrderStatus;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity(name = "orders")
@Table(name = "orders")
@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
public class DbOrder {

    @Id
    private String id;
    private String customerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ElementCollection
    private List<String> addingProducts = new ArrayList<>();
    @ElementCollection
    private List<String> removeProducts = new ArrayList<>();
    private BigDecimal finalPrice;
    private String customer;
}
