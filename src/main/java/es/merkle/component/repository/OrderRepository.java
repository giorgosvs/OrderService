package es.merkle.component.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import es.merkle.component.repository.entity.DbOrder;

@Repository
public interface OrderRepository extends JpaRepository<DbOrder, String> {
}
