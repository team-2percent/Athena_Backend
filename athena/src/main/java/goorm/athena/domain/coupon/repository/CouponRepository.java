package goorm.athena.domain.coupon.repository;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByIdAndCouponStatus(Long id, CouponStatus couponStatus);

    List<Coupon> findByCouponStatusAndStartAtLessThanEqual(CouponStatus status, LocalDateTime date);

    List<Coupon> findByCouponStatusNotAndEndAtLessThan(CouponStatus status, LocalDateTime dateTime);
}
