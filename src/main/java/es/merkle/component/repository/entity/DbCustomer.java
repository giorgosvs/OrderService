package es.merkle.component.repository.entity;

import javax.persistence.Table;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "customers")
@Table(name = "customers")
@Getter
@Setter
@SuperBuilder
@RequiredArgsConstructor
@AllArgsConstructor
public class DbCustomer {

    @Id
    @UuidGenerator
    private String id;
    private String name;
    private String address;
    private String phoneNumber;

    @ManyToMany //added JPA Relationship Many to Many
    @JoinTable(name = "customer_owned_products" //Created junction table product_id <-> customer_id
            , joinColumns = @JoinColumn(name = "customer_id")
            , inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<DbProduct> ownedProducts = new ArrayList<>();
}
