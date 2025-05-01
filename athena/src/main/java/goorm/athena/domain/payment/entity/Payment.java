package goorm.athena.domain.payment.entity;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
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
    private int amountTotal;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;

}