package goorm.athena.domain.payment.repository;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    @Query("""
    SELECT p.order FROM Payment p
    WHERE p.order.project IN :projects
      AND p.order.isSettled = false
      AND p.status = 'APPROVED'
      AND p.order.orderedAt BETWEEN p.order.project.startAt AND p.order.project.endAt
    """)
    List<Order> findUnsettledOrdersByProjects(@Param("projects") List<Project> projects);
}