package goorm.athena.domain.coupon.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.entity.QCoupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CouponQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    // 발급일 Or 만료일이 지난 상태를 업데이트 해야할 쿠폰을 조회함 (스케줄링)
    public List<Coupon> findCouponsToUpdate(LocalDateTime now) {
        QCoupon coupon = QCoupon.coupon;

        return jpaQueryFactory
                .selectFrom(coupon)
                .where(
                        coupon.startAt.loe(now)
                                .or(coupon.endAt.loe(now))
                )
                .fetch();
    }

    // 발급 진행중인 모든 쿠폰들을 조회
    public List<Coupon> findAllInProgressCoupons(){
        QCoupon coupon = QCoupon.coupon;

        return jpaQueryFactory
                .selectFrom(coupon)
                .where(coupon.couponStatus.eq(CouponStatus.IN_PROGRESS))
                .orderBy(coupon.id.desc())
                .fetch();
    }
}
