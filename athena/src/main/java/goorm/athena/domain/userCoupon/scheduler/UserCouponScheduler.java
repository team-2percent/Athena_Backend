package goorm.athena.domain.userCoupon.scheduler;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
import goorm.athena.domain.userCoupon.repository.UserCouponQueryRepository;
import goorm.athena.domain.userCoupon.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCouponScheduler {
    private final UserCouponRepository userCouponRepository;
    private final UserCouponQueryRepository userCouponQueryRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void expiredUserCoupon(){
        LocalDateTime now = LocalDateTime.now();

        List<UserCoupon> allUserCoupons = userCouponQueryRepository.findAllWithCoupon();

        for(UserCoupon userCoupon : allUserCoupons){
            Coupon coupon = userCoupon.getCoupon();

            if(coupon.getExpiresAt().isBefore(now)){
                userCoupon.setExpired();
            }
        }

        userCouponRepository.saveAll(allUserCoupons);
    }


}
