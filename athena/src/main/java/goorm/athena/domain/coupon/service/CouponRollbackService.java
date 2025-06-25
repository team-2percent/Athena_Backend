package goorm.athena.domain.coupon.service;

import goorm.athena.domain.coupon.infra.CouponRollbackOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponRollbackService {

    private static final int MAX_RETRY = 5;

    private final CouponRollbackOperation couponRollbackOperation;

    public void rollback(Long couponId){
        int retryCount = 0;
        boolean success = false;

        while (retryCount < MAX_RETRY && !success){
            try{
                couponRollbackOperation.rollbackRedisStock(couponId);
                success = true;
            } catch (Exception e){
                retryCount++;
                try{
                    Thread.sleep(1000L * retryCount);
                } catch (InterruptedException ie){
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        if(!success){
            couponRollbackOperation.alertAdmin("Redis Rollback failed : " + couponId);
        }
    }
}
