package es.merkle.component.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@RequiredArgsConstructor
@ToString
public class Customer {
    private String id;
    private String name;
    private String address;
    private String phoneNumber;
    private final List<Product> ownedProducts = new ArrayList<>();
}
