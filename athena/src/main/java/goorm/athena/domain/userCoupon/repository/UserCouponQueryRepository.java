package goorm.athena.domain.userCoupon.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.coupon.entity.QCoupon;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.userCoupon.dto.res.UserCouponGetResponse;
import goorm.athena.domain.userCoupon.entity.QUserCoupon;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.mapper.UserCouponMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class UserCouponQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final UserCouponMapper userCouponMapper;

    // 마이 페이지의 보유 쿠폰을 무한 페이지 조회
    public UserCouponCursorResponse getUserCouponByCursor(Long userId, Long cursorId, int size){
        QUserCoupon userCoupon = QUserCoupon.userCoupon;
        QCoupon coupon = QCoupon.coupon;

        BooleanBuilder condition = new BooleanBuilder();
        condition.and(userCoupon.user.id.eq(userId));
        if(cursorId != null){
            condition.and(userCoupon.id.gt(cursorId));
        }

        List<UserCouponGetResponse> content = queryFactory
                .select(Projections.constructor(
                        UserCouponGetResponse.class,
                        userCoupon.id,
                        coupon.id,
                        coupon.title,
                        coupon.content,
                        coupon.price,
                        coupon.stock,
                        coupon.expiresAt,
                        userCoupon.status
                )).from(userCoupon)
                .join(userCoupon.coupon, coupon)
                .where(condition)
                .orderBy(userCoupon.id.asc())
                .limit(size)
                .fetch();

        Long total = queryFactory
                .select(userCoupon.count())
                .from(userCoupon)
                .where(userCoupon.user.id.eq(userId))
                .fetchOne();

        Long nextCursor = content.isEmpty() ? null : content.get(content.size() - 1).id();

        return userCouponMapper.toGetCursorResponse(content, nextCursor, total);
    }

    // 유저가 보유한 모든 쿠폰을 조회 (스케줄링에서 사용)
    public List<UserCoupon> findAllWithCoupon(){
        QUserCoupon userCoupon = QUserCoupon.userCoupon;
        QCoupon coupon = QCoupon.coupon;

        return queryFactory
                .selectFrom(userCoupon)
                .join(userCoupon.coupon, coupon).fetchJoin() // ← fetch join 사용
                .fetch();
    }

    // 유저가 발급한 쿠폰이 있는 쿠폰 Id들만을 조회
    public Set<Long> findCouponIdsByUserIdAndCouponIdIn(Long userId, List<Long> couponIds){
        QUserCoupon userCoupon = QUserCoupon.userCoupon;

        return new HashSet<>(queryFactory
                .select(userCoupon.coupon.id)
                .from(userCoupon)
                .where(
                        userCoupon.user.id.eq(userId),
                        userCoupon.coupon.id.in(couponIds)
                )
                .fetch());
    }

}
