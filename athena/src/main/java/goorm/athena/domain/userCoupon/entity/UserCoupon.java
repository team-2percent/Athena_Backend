package goorm.athena.domain.userCoupon.entity;

import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "userCoupon")
public class UserCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime issueAt;

    @Builder
    private UserCoupon(User user, Coupon coupon){
        this.user = user;
        this.coupon = coupon;
        this.status = Status.UNUSED;
        this.issueAt = LocalDateTime.now();
    }

    public static UserCoupon create(User user, Coupon coupon){
        return new UserCoupon(user, coupon);
    }

}
