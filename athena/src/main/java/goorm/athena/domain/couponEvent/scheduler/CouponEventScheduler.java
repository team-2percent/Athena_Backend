package goorm.athena.domain.couponEvent.scheduler;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.couponEvent.entity.CouponEvent;
import goorm.athena.domain.couponEvent.repository.CouponEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CouponEventScheduler {

    private final CouponEventRepository couponEventRepository;

    @Transactional
    @Scheduled(cron = "1 0 0 * * *")
    public void updateCouponEventStatus(){
        LocalDateTime now = LocalDateTime.now();

        List<CouponEvent> allEvents = couponEventRepository.findAllWithCoupon();

        for(CouponEvent event : allEvents){
            Coupon coupon = event.getCoupon();

            if(coupon.getEndAt().isBefore(now)){
                // 만료된 경우
                if(event.isActive()){
                    event.setInactive();
                }
            } else if(!coupon.getStartAt().isAfter(now)){
                // 발급 시작일 활성화
                if(!event.isActive()){
                    event.setActive();
                }
            }
        }

        couponEventRepository.saveAll(allEvents);
    }
}
