package goorm.athena.domain.product.entity;

import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User sellerId;

    private Long categoryId;
    private String title;
    private String description;
    private Long goalAmount;
    private Long totalAmount;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime shippedAt;

    @Enumerated(EnumType.STRING)
    private Status status;



}