package es.merkle.component.repository.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Table;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity(name = "products")
@Table(name = "products")
@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
public class DbProduct {
    @Id
    private String id;
    private String name;
    private String productStatus;
    private String productCategory;
    private BigDecimal price;
    private LocalDate expiringDate;
    private LocalDate releasedDate;
}
