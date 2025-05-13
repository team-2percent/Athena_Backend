package goorm.athena.domain.order.repository;


import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
    SELECT p.order FROM Payment p
    WHERE p.order.project IN :projects
      AND p.order.isSettled = false
      AND p.status = 'APPROVED'
      AND p.order.orderedAt BETWEEN p.order.project.startAt AND p.order.project.endAt
    """)
    List<Order> findUnsettledOrdersForProjects(@Param("projects") List<Project> projects);
}