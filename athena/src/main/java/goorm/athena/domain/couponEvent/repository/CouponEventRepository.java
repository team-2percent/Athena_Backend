package goorm.athena.domain.couponEvent.repository;

import goorm.athena.domain.couponEvent.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
    @Query("SELECT e FROM CouponEvent e JOIN FETCH e.coupon")
    List<CouponEvent> findAllWithCoupon();

    List<CouponEvent> findByIsActiveTrue();

}
