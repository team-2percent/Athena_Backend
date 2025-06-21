package goorm.athena.domain.coupon.infra;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponQueryService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CouponSyncOperation {

    private final RedissonClient redissonClient;
    private final CouponQueryService couponQueryService;
    private final CouponRepository couponRepository;

    @Transactional
    public void syncCouponStock(Long couponId){
        Coupon coupon = couponQueryService.getCoupon(couponId);

        String totalKey = "coupon_total_" + couponId;
        String usedKey = "coupon_used_" + couponId;

        RBucket<String> totalBucket = redissonClient.getBucket(totalKey, StringCodec.INSTANCE);
        RBucket<String> usedBucket = redissonClient.getBucket(usedKey, StringCodec.INSTANCE);

        int total = Integer.parseInt(totalBucket.get());
        int used = Integer.parseInt(usedBucket.get());

        int remainingStock = Math.max(0, total - used);
        coupon.stockSync(remainingStock); // 재고 설정

        if (remainingStock == 0 && coupon.getCouponStatus() == CouponStatus.IN_PROGRESS) {
            coupon.completed();
        }

        couponRepository.save(coupon);
    }
}
