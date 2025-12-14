package es.merkle.component.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import es.merkle.component.repository.entity.DbOrder;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<DbOrder, String> {
    @Query("""
    SELECT o FROM orders o
    LEFT JOIN FETCH o.items i
    LEFT JOIN FETCH i.product
    WHERE o.id = :id
""")
    Optional<DbOrder> findByIdWithItems(@Param("id") String id);

}
