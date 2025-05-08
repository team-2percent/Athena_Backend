package goorm.athena.domain.coupon.entity;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@RequiredArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long code;
    private String title;
    private String content;
    private int price;

    private LocalDateTime startAt;
    private LocalDateTime endAt;

    private LocalDateTime expiresAt;
    private int stock;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Builder
    public static Coupon create(CouponCreateRequest request){
        Coupon coupon = new Coupon();
        coupon.code = request.code();
        coupon.title = request.title();
        coupon.content = request.content();
        coupon.price = request.price();
        coupon.startAt = request.startAt();
        coupon.endAt = request.endAt();
        coupon.expiresAt = request.expiresAt();
        coupon.stock = request.stock();
        coupon.status = Status.PREVIOUS;
        return coupon;
    }
}