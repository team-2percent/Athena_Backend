package goorm.athena.domain.couponEvent.repository;

import goorm.athena.domain.couponEvent.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
}
