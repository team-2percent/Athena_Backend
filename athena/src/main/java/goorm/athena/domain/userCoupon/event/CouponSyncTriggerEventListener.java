package goorm.athena.domain.userCoupon.event;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponQueryService;
import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponSyncTriggerEventListener {

    private final CouponQueryService couponQueryService;
    private final CouponRepository couponRepository;
    private final RedissonClient redissonClient;

    @Async
    @EventListener
    public void handleCouponSyncTriggerEvent(CouponSyncTriggerEvent event) {
        Long couponId = event.couponId();
        Coupon coupon = couponQueryService.getCoupon(couponId);

        String totalKey = "coupon_total_" + couponId;
        String usedKey = "coupon_used_" + couponId;

        RBucket<String> totalBucket = redissonClient.getBucket(totalKey, StringCodec.INSTANCE);
        RBucket<String> usedBucket = redissonClient.getBucket(usedKey, StringCodec.INSTANCE);

        int total = Integer.parseInt(totalBucket.get());
        int used = Integer.parseInt(usedBucket.get());

        int remainingStock = Math.max(0, total - used);
        if (remainingStock == 0 && coupon.getCouponStatus() == CouponStatus.IN_PROGRESS) {
            coupon.markAsSoldOut(remainingStock); // 재고 설정
            coupon.completed();
        } else {
            couponRepository.save(coupon);
        }
    }
}

