package goorm.athena.domain.coupon.repository;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Query("SELECT c FROM Coupon c WHERE c.startAt <= :now OR c.endAt <= :now")
    List<Coupon> findCouponsToUpdate(LocalDateTime now);

    Page<Coupon> findByCouponStatus(Pageable pageable, CouponStatus status);
}
