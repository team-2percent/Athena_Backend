package goorm.athena.domain.userCoupon.repository;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    boolean existsByUserAndCoupon(User user, Coupon coupon);

    Optional<UserCoupon> findByIdAndUser(Long userCouponId, User user);

    List<UserCoupon> findByUser(User user);

    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon")
    List<UserCoupon> findAllWithCoupon();
}
