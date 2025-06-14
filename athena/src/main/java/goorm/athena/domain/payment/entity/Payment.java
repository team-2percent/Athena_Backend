package goorm.athena.domain.payment.entity;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String tid;
    private String pgToken;
    private Long amountTotal;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

    public static Payment create(Order order, User user, String tid, Long amountTotal) {
        Payment payment = new Payment();
        payment.order = order;
        payment.user = user;
        payment.tid = tid;
        payment.amountTotal = amountTotal;
        payment.status = Status.PENDING;
        payment.createdAt = LocalDateTime.now();
        return payment;
    }

    public void approve(String pgToken) {
        if (this.status != Status.PENDING) {
            throw new IllegalStateException("결제는 READY 상태에서만 승인될 수 있습니다.");
        }
        this.pgToken = pgToken;
        this.approvedAt = LocalDateTime.now();
        this.status = Status.APPROVED;
    }

}