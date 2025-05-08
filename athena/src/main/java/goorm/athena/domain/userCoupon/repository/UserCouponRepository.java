package goorm.athena.domain.userCoupon.repository;

import goorm.athena.domain.userCoupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
}
