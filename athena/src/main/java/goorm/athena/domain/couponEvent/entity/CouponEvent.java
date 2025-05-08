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

    private String title;
    private String content;
    boolean isActive;

    @Builder
    public static CouponEvent create(CouponEventCreateRequest request, Coupon coupon){
        CouponEvent couponEvent = new CouponEvent();
        couponEvent.coupon = coupon;
        couponEvent.title = request.title();
        couponEvent.content = request.content();
        couponEvent.isActive = false;
        return couponEvent;
    }
}
