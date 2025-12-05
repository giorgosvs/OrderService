package es.merkle.component.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import es.merkle.component.repository.entity.DbProduct;

@Repository
public interface ProductRepository extends JpaRepository<DbProduct, String> {
}
