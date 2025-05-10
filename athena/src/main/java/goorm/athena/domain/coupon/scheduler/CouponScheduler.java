package goorm.athena.domain.coupon.scheduler;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

         // 1. PREVIOUS → IN_PROGRESS
         List<Coupon> toActivate = couponRepository.findByCouponStatusAndStartAtLessThanEqual(CouponStatus.PREVIOUS, now);
         toActivate.forEach(Coupon::active);

         // 2. NOT ENDED → ENDED
         List<Coupon> toEnd = couponRepository.findByCouponStatusNotAndEndAtLessThan(CouponStatus.ENDED, now);
         toEnd.forEach(Coupon::expired);

         couponRepository.saveAll(toActivate);
         couponRepository.saveAll(toEnd);
     }
}
