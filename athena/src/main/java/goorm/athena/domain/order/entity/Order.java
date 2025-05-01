package goorm.athena.domain.order.entity;

import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private User buyer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String itemName;
    private int quantity;
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    private Status paymentStatus;

    private LocalDateTime orderedAt;

}
