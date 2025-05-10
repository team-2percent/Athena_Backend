package goorm.athena.domain.coupon.scheduler;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

    private final CouponRepository couponRepository;

     // 추후 bulk update ( 한 번에 업데이트 ) 보다는 대용량 처리에 적합한 batch로 쿼리 조회 수 리팩토링 예정
     @Transactional
     @Scheduled(cron = "2 0 0 * * *")
     public void updateCouponStatuses(){
         LocalDateTime now = LocalDateTime.now();

         // 발급일 or 만료일이 지난 상태를 업데이트 해야할 쿠폰들만 조회
         List<Coupon> coupons = couponRepository.findCouponsToUpdate(now);

         List<Coupon> updatedCoupons = new ArrayList<>();

         for (Coupon coupon : coupons) {
             // 1. PREVIOUS -> IN_PROGRESS
             if (coupon.getCouponStatus() == CouponStatus.PREVIOUS && !coupon.getStartAt().isAfter(now)) {
                 coupon.active();
                 updatedCoupons.add(coupon);
             } // 2. NOT ENDED -> ENDED
             else if (coupon.getCouponStatus() != CouponStatus.ENDED && coupon.getEndAt().isBefore(now)) {
                 coupon.expired();
                 updatedCoupons.add(coupon);
             }
         }

         if (!updatedCoupons.isEmpty()) {
             couponRepository.saveAll(updatedCoupons);
         }
     }
}
