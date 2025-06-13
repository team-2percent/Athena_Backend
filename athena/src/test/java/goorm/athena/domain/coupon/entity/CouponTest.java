package goorm.athena.domain.coupon.entity;

import goorm.athena.domain.coupon.util.CouponIntegrationTestSupport;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest extends CouponIntegrationTestSupport {

    @DisplayName("쿠폰의 재고가 0 이하일 때, 재고가 감소될 경우 에러를 리턴한다.")
    @Test
    void decreaseStock_COUPON_OUT_STOCK() {
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                -1, CouponStatus.IN_PROGRESS);

        assertThatThrownBy(() -> coupon.decreaseStock())
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.COUPON_OUT_STOCK.getErrorMessage());
    }

    @DisplayName("쿠폰의 재고가 1에서 0이 되었을 때, 모든 재고가 소진되어 현재 쿠폰의 상태를 '발급 완료'로 변경한다.")
    @Test
    void coupon_COMPLETED(){
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                1, CouponStatus.IN_PROGRESS);

        coupon.decreaseStock();

        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.COMPLETED);
    }

    @DisplayName("현재 쿠폰의 상태를 '발급 중' 상태로 변경한다.")
    @Test
    void active() {
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                1, CouponStatus.PREVIOUS);

        coupon.active();

        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.IN_PROGRESS);
    }

    @DisplayName("현재 쿠폰의 상태를 '발급 종료' 상태로 변경한다.")
    @Test
    void expired() {
        Coupon coupon = setupCoupon("123", "123123123123", 10000, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(),
                1, CouponStatus.IN_PROGRESS);

        coupon.expired();

        assertThat(coupon.getCouponStatus()).isEqualTo(CouponStatus.ENDED);
    }

    protected Coupon setupCoupon(String title, String content, int price, LocalDateTime startAt,
                                 LocalDateTime endAt, LocalDateTime expiresAt, int stock, CouponStatus couponStatus){
        return TestEntityFactory.createCoupon(title, content, price, startAt, endAt, expiresAt, stock, couponStatus);
    }
}