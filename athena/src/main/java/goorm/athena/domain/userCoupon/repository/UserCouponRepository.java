package goorm.athena.domain.userCoupon.repository;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserAndCoupon(User user, Coupon coupon);

    Optional<UserCoupon> findByIdAndUser(Long userCouponId, User user);

    List<UserCoupon> findByUser(User user);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon")
    List<UserCoupon> findAllWithCoupon();

    @Query("SELECT uc.coupon.id FROM UserCoupon uc WHERE uc.user.id = :userId AND uc.coupon.id IN :couponIds")
    Set<Long> findCouponIdsByUserIdAndCouponIdIn(Long userId, List<Long> couponIds);
}
