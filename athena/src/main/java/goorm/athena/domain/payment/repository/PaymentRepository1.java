package goorm.athena.domain.payment.repository;

import goorm.athena.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository1 extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

}
