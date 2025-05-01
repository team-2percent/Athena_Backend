package goorm.athena.domain.coupon.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long code;
    private String name;
    private String content;
    private int price;

    private LocalDateTime expiresAt;
    private int stock;

}