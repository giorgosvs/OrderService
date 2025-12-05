package es.merkle.component.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private ProductStatus productStatus;
    private ProductCategory productCategory;
    private BigDecimal price;
    private LocalDate expiringDate;
    private LocalDate releasedDate;
}
