package goorm.athena.domain.payment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class StockHistory {
    @Id
    @GeneratedValue
    private Long id;

    private Long productId;

    private Long userId;

    private Long orderId;

    private Integer deductedQuantity;

    private Boolean synced = false;

    private LocalDateTime createdAt = LocalDateTime.now();

}