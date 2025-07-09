package goorm.athena.domain.coupon.infra;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponSyncOperation {

    private final RedissonClient redissonClient;
    private final CouponQueryService couponQueryService;
    private final CouponRepository couponRepository;

    @Retryable(
            value = { RuntimeException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
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

    @Recover
    public void recover(RuntimeException e, Long couponId) {
        String metaKey = "coupon_meta_" + couponId;
        RMap<String, String> couponMeta = redissonClient.getMap(metaKey, StringCodec.INSTANCE);

        String totalKey = couponMeta.get("total");
        String usedKey = couponMeta.get("used");

        log.error("쿠폰 재고 동기화 최종 실패: couponId={}, 총 재고={}, 사용량={}, 에러={}",
                couponId, totalKey, usedKey, e.getMessage());

        // 쿠폰 상태를 SYNC_FAILED로 변경하여 서비스에서 사용 금지 처리
        log.info("recover - 쿠폰 상태 변경 시작");
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new RuntimeException("쿠폰을 찾을 수 없습니다: " + couponId));
        coupon.syncFailed();
        couponRepository.save(coupon);
        log.info("recover - 쿠폰 상태 변경 완료");
    }
}
