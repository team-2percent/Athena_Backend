package goorm.athena.domain.settlement.entity;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bankaccount_id")
    private BankAccount bankAccount;

    private int totalCount;
    private int totalSales;

    private LocalDateTime settledAt;
    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
}