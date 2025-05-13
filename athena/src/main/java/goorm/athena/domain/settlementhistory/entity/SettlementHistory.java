package goorm.athena.domain.settlementhistory.entity;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.settlement.entity.Settlement;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class SettlementHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name ="total_price")
    private long totalPrice;
    private int fee;
    private Long amount;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder
    public SettlementHistory(Settlement settlement, Order order, long totalPrice, int fee, Long amount) {
        this.settlement = settlement;
        this.order = order;
        this.totalPrice = totalPrice;
        this.fee = fee;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
}