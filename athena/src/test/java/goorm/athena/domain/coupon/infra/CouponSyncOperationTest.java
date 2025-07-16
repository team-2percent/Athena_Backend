package goorm.athena.domain.coupon.infra;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.util.CouponInfraIntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CouponSyncOperationTest extends CouponInfraIntegrationTestSupport {

    @DisplayName("쿠폰 재고 동기 과정에서 DB 저장에 오류가 발생하면 3번 재시도 + 1회 복구로 총 4번 저장이 호출됐는지 검증합니다.")
    @Test
    void testRetryAndRecover() {
        // save() 호출 3번은 예외 발생, 4번째 호출 때는 정상 Coupon 객체 반환하도록 설정
        when(couponRepository.save(any()))
                .thenThrow(new RuntimeException("강제 실패"))
                .thenThrow(new RuntimeException("강제 실패"))
                .thenThrow(new RuntimeException("강제 실패"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        couponSyncOperation.syncCouponStock(testCouponId);

        Coupon updateCoupon = couponRepository.findById(testCouponId).orElseThrow();
        assertEquals(CouponStatus.SYNC_FAILED, updateCoupon.getCouponStatus());

        // save()가 총 4번 호출됐는지 검증
        verify(couponRepository, times(4)).save(any());
    }

    @DisplayName("쿠폰 재고 동기 중 2번 재시도 후 정상 저장(동기) 시 총 3번 저장이 호출됐는지 확인 및 재고 동기 상태를 검증합니다.")
    @Test
    void testTwoRetry() {
        when(couponRepository.save(any()))
                .thenThrow(new RuntimeException("강제 실패"))
                .thenThrow(new RuntimeException("강제 실패"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        couponSyncOperation.syncCouponStock(testCouponId);

        // save()가 총 3번 호출됐는지 검증 (재시도 2회 + 정상 1회)
        verify(couponRepository, times(3)).save(any());


        Coupon updatedCoupon = couponRepository.findById(testCouponId).orElseThrow();

        int expectedRemainingStock = 80;
        assertEquals(expectedRemainingStock, updatedCoupon.getStock());

        assertEquals(CouponStatus.IN_PROGRESS, updatedCoupon.getCouponStatus());
    }

}