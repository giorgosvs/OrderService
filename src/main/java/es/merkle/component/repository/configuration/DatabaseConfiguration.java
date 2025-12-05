package es.merkle.component.repository.configuration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import es.merkle.component.repository.CustomerRepository;
import es.merkle.component.repository.OrderRepository;
import es.merkle.component.repository.ProductRepository;
import es.merkle.component.repository.entity.DbCustomer;
import es.merkle.component.repository.entity.DbProduct;

@Configuration
public class DatabaseConfiguration {

    @Bean
    CommandLineRunner init(CustomerRepository customerRepository, OrderRepository orderRepository, ProductRepository productRepository) {
        return args -> {

            customerRepository.saveAll(List.of(
                    DbCustomer.builder()
                            .name("Joel")
                            .address("Av Camp Nou, 199")
                            .phoneNumber("839-758-3859")
                            .build(),
                    DbCustomer.builder()
                            .name("Edgard")
                            .address("Camino del Mar, 82")
                            .phoneNumber("529-635-3994")
                            .build(),
                    DbCustomer.builder()
                            .name("Joan")
                            .address("Calle Paraguay, 3")
                            .phoneNumber("586-839-9934")
                            .build()
            ));

            productRepository.saveAll(List.of(
                    DbProduct.builder()
                            .id("NITFLIX")
                            .name("The real Nitflix")
                            .price(BigDecimal.valueOf(12.99))
                            .productCategory("TV")
                            .productStatus("AVAILABLE")
                            .releasedDate(LocalDate.of(2023, 5, 10))
                            .expiringDate(LocalDate.now().plusMonths(6))
                            .build(),
                    DbProduct.builder()
                            .id("SPITIFY")
                            .name("Spitify")
                            .price(BigDecimal.valueOf(6.99))
                            .productCategory("TV")
                            .productStatus("NOT AVAILABLE")
                            .releasedDate(LocalDate.of(2023, 5, 10))
                            .expiringDate(LocalDate.now().plusMonths(6))
                            .build(),
                    DbProduct.builder()
                            .id("YITIP")
                            .name("Yitip")
                            .price(BigDecimal.valueOf(2.99))
                            .productCategory("TV")
                            .productStatus("VIP")
                            .releasedDate(LocalDate.of(2023, 5, 10))
                            .expiringDate(LocalDate.now().plusMonths(6))
                            .build(),
                    DbProduct.builder()
                            .id("DISNY")
                            .name("Yitip")
                            .price(BigDecimal.valueOf(2.99))
                            .productCategory("TV")
                            .productStatus("AVAILABLE")
                            .releasedDate(LocalDate.of(2025, 5, 10))
                            .expiringDate(LocalDate.now().plusMonths(6))
                            .build()
            ));
        };
    }
}
