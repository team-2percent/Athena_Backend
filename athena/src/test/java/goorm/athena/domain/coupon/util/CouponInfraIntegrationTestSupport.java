package goorm.athena.domain.coupon.util;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.infra.CouponSyncOperation;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.domain.coupon.service.CouponQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CouponSyncOperation.class})
@EnableRetry
public abstract class CouponInfraIntegrationTestSupport {

    @Autowired
    protected CouponSyncOperation couponSyncOperation;

    @MockBean
    protected RedissonClient redissonClient;

    @MockBean
    protected RKeys rKeys;

    @MockBean
    protected CouponQueryService couponQueryService;

    @MockBean
    protected CouponRepository couponRepository;

    @MockBean
    protected RMap<String, String> couponMeta;

    protected final Long testCouponId = 18L;

    @BeforeEach
    void setup() {
        CouponCreateRequest request = new CouponCreateRequest(
                "테스트 쿠폰 제목",
                "테스트 내용입니다",
                1000,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(20),
                100  // stock
        );

        Coupon coupon = Coupon.create(request);
        coupon.active();

        // Mockito stubbing
        when(couponQueryService.getCoupon(testCouponId)).thenReturn(coupon);
        when(couponRepository.findById(testCouponId))
                .thenReturn(Optional.of(coupon));

        // RedisMap 반환 세팅
        when(redissonClient.getMap("coupon_meta_" + testCouponId, StringCodec.INSTANCE))
                .thenReturn((RMap) couponMeta);

        // RedisMap 내 값 세팅
        when(couponMeta.get("total")).thenReturn(String.valueOf(coupon.getStock()));
        when(couponMeta.get("used")).thenReturn("20");

        when(redissonClient.getKeys()).thenReturn(rKeys);
    }
}
