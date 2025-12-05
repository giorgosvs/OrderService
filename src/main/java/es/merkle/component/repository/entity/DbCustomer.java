package es.merkle.component.repository.entity;

import javax.persistence.Table;
import org.hibernate.annotations.UuidGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
}
