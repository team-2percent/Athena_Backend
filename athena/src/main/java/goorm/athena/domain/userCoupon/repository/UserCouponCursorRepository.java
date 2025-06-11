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

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserCouponCursorRepository {
    private final JPAQueryFactory queryFactory;
    private final UserCouponMapper userCouponMapper;

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

}
