package goorm.athena.domain.couponEvent.repository;

import goorm.athena.domain.couponEvent.entity.CouponEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CouponEventRepository extends JpaRepository<CouponEvent, Long> {
    // 스케줄러 쿠폰 이벤트 상태 업데이트 할 때 사용
    @Query("SELECT e FROM CouponEvent e JOIN FETCH e.coupon")
    List<CouponEvent> findAllWithCoupon();

    // 쿠폰 이벤트에서 활성화 된 이벤트들만 조회할 때 사용
    @Query("SELECT e FROM CouponEvent e JOIN FETCH e.coupon WHERE e.isActive = true")
    List<CouponEvent> findByIsActiveTrueWithCoupon();

}
