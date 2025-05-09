package goorm.athena.domain.coupon.scheduler;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

    private final CouponRepository couponRepository;

     @Scheduled(cron = "2 0 0 * * *")
     public void updateCouponStatuses(){
         LocalDateTime now = LocalDateTime.now();

         // 1. PREVIOUS → IN_PROGRESS
         List<Coupon> toActivate = couponRepository.findByCouponStatusAndStartAtLessThanEqual(CouponStatus.PREVIOUS, now);
         toActivate.forEach(Coupon::active);

         // 2. NOT ENDED → ENDED
         List<Coupon> toEnd = couponRepository.findByCouponStatusNotAndEndAtLessThan(CouponStatus.ENDED, now);
         toEnd.forEach(Coupon::expired);

         // 저장
         couponRepository.saveAll(toActivate);
         couponRepository.saveAll(toEnd);

         System.out.println("쿠폰 상태 업데이트 완료 (대기→발급중, 발급중→종료)");
     }
}
