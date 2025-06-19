package goorm.athena.domain.coupon.scheduler;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponQueryRepository;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.userCoupon.event.CouponSyncTriggerEvent;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponScheduler {

    private final CouponQueryRepository couponQueryRepository;
    private final CouponRepository couponRepository;

    private final RedissonClient redissonClient;
    private final ApplicationEventPublisher eventPublisher;

     // 추후 bulk update ( 한 번에 업데이트 ) 보다는 대용량 처리에 적합한 batch로 쿼리 조회 수 리팩토링 예정

     @Transactional
     @Scheduled(cron = "2 0 0 * * *")
     public void updateCouponStatuses(){
         LocalDateTime now = LocalDateTime.now();

         // 발급일 or 만료일이 지난 상태를 업데이트 해야할 쿠폰들만 조회
         List<Coupon> coupons = couponQueryRepository.findCouponsToUpdate(now);
         List<Coupon> updatedCoupons = new ArrayList<>();

         for (Coupon coupon : coupons) {
             // 1. PREVIOUS -> IN_PROGRESS
             if (coupon.getCouponStatus() == CouponStatus.PREVIOUS &&
                     !coupon.getStartAt().isAfter(now) && !coupon.getEndAt().isBefore(now)) {
                 coupon.active();
                 updatedCoupons.add(coupon);

                 redissonClient.getAtomicLong("coupon_total_" + coupon.getId()).set(coupon.getStock()); // DB 기준
                 redissonClient.getAtomicLong("coupon_used_" + coupon.getId()).set(0L);
                 redissonClient.getSet("issued_users_" + coupon.getId()).clear();

             } // 2. NOT ENDED -> ENDED
             if (coupon.getCouponStatus() != CouponStatus.ENDED && coupon.getEndAt().isBefore(now)) {
                 coupon.expired();
                 updatedCoupons.add(coupon);
             }
         }

         if (!updatedCoupons.isEmpty()) {
             couponRepository.saveAll(updatedCoupons);
         }
     }

    // 10초마다 실행, fixedDelay는 이전 작업 종료 후 대기 시간
    @Scheduled(fixedDelay = 10000)
    public void publishCouponSyncEvents() {
        // 활성 쿠폰 ID 리스트 조회 (예: 상태가 ACTIVE인 쿠폰만)
        List<Long> activeCouponIds = couponRepository.findCouponIdsByStatus(CouponStatus.IN_PROGRESS);

        for (Long couponId : activeCouponIds) {
            // 이벤트 발행
            eventPublisher.publishEvent(new CouponSyncTriggerEvent(couponId));
        }
    }
}
