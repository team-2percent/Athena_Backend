    package goorm.athena.domain.userCoupon.infra;

    import goorm.athena.domain.coupon.event.CouponSyncTriggerEvent;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.redisson.api.RMap;
    import org.redisson.api.RedissonClient;
    import org.redisson.client.codec.StringCodec;
    import org.springframework.context.ApplicationEventPublisher;
    import org.springframework.stereotype.Component;

    import javax.annotation.PostConstruct;

    @Slf4j
    @Component
    @RequiredArgsConstructor
    public class RedisKeyExpirationListener {

        private final RedissonClient redissonClient;
        private final ApplicationEventPublisher publisher;

        @PostConstruct
        public void registerExpirationListener() {
            // Redis 키 TTL 만료 이벤트 구독 ( DB 0 기준 )
            redissonClient.getTopic("__keyevent@0__:expired", StringCodec.INSTANCE)
                    .addListener(String.class, (channel, expiredKey) -> {
                        log.info("Redis TTL 만료 감지: {}", expiredKey);

                        if (expiredKey.startsWith("coupon_ttl_")) {
                            try {
                                Long couponId = Long.parseLong(expiredKey.replace("coupon_ttl_", ""));

                                String metaKey = "coupon_meta_" + couponId;
                                RMap<String, String> couponMeta = redissonClient.getMap(metaKey, StringCodec.INSTANCE);

                                String syncTriggered = couponMeta.getOrDefault("sync_triggered", "0");
                                if ("1".equals(syncTriggered)) {
                                    log.info("이미 동기화된 쿠폰입니다. 이벤트 발행하지 않음: couponId={}", couponId);
                                    return;
                                }

                                log.info("TTL 만료로 쿠폰 동기화 이벤트 발행: couponId={}", couponId);
                                publisher.publishEvent(new CouponSyncTriggerEvent(couponId));
                            } catch (Exception e) {
                                log.error("쿠폰 ID 파싱 실패: {}", expiredKey, e);
                            }
                        }
                    });
        }
    }
