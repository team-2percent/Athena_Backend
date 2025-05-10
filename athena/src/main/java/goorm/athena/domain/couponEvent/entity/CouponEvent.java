package goorm.athena.domain.couponEvent.entity;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.couponEvent.dto.req.CouponEventCreateRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "couponEvent")
public class CouponEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    private boolean isActive;

    @Builder
    public static CouponEvent create(Coupon coupon){
        CouponEvent couponEvent = new CouponEvent();
        couponEvent.coupon = coupon;
        couponEvent.isActive = false;
        return couponEvent;
    }

    public void setActive(){
        this.isActive = true;
    }

    public void setInactive(){
        this.isActive = false;
    }
}
