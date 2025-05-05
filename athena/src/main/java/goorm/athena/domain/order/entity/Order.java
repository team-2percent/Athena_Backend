package goorm.athena.domain.order.entity;

import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    private DeliveryInfo delivery;

    private String itemName;
    private int quantity;
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime orderedAt;


    public static Order create(User user, DeliveryInfo delivery, LocalDateTime orderedAt) {
        Order order = new Order();
        order.user = user;
        order.delivery = delivery;
        order.orderedAt = orderedAt;
        order.status = Status.ORDERED;
        return order;
    }

    public void completeOrder(int totalPrice) {
        this.totalPrice = totalPrice;
        this.status = Status.ORDERED;
    }
}
