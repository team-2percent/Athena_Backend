package goorm.athena.domain.userCoupon.scheduler;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.userCoupon.entity.UserCoupon;
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

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void expiredUserCoupon(){
        LocalDateTime now = LocalDateTime.now();

        List<UserCoupon> allUserCoupons = userCouponRepository.findAllWithCoupon();

        for(UserCoupon userCoupon : allUserCoupons){
            Coupon coupon = userCoupon.getCoupon();

            if(coupon.getExpiresAt().isBefore(now)){
                userCoupon.setExpired();
            }
        }

        userCouponRepository.saveAll(allUserCoupons);
        System.out.println("유저의 쿠폰을 만료시키는 스케줄러");
    }


}
