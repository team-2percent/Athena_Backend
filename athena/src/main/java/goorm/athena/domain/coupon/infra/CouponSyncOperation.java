package goorm.athena.domain.coupon.infra;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponQueryService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
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

        String metaKey = "coupon_meta_" + couponId;
        RMap<String, String> couponMeta = redissonClient.getMap(metaKey, StringCodec.INSTANCE);

        String totalKey = couponMeta.get("total");
        String usedKey = couponMeta.get("used");

        int total = Integer.parseInt(totalKey);
        int used = usedKey != null ? Integer.parseInt(usedKey) : 0;

        int remainingStock = Math.max(0, total - used);
        coupon.stockSync(remainingStock); // 재고 설정

        if (remainingStock == 0 && coupon.getCouponStatus() == CouponStatus.IN_PROGRESS) {
            coupon.completed();
        }

        couponRepository.save(coupon);
    }
}
