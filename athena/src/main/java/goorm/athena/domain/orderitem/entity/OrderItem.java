package goorm.athena.domain.orderitem.entity;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.product.entity.Product;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int quantity;
    private Long price;

    @Builder
    public OrderItem(Order order, Product product, int quantity, Long price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public static OrderItem of(Order order, Product product, int quantity) {
        Long price = product.getProductPrice() * quantity;
        return new OrderItem(order, product, quantity, price);
    }

}