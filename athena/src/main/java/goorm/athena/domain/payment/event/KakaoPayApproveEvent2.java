package goorm.athena.domain.payment.event;

import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.service.RedisStockDeductionResult;
import lombok.Getter;

import java.util.List;

@Getter
public class KakaoPayApproveEvent2 {
    private final Payment payment;
    private final String pgToken;
    private final List<OrderItem> orderItems;
    private final RedisStockDeductionResult deductedStocks;

    public KakaoPayApproveEvent2(Payment payment, String pgToken, List<OrderItem> orderItems, RedisStockDeductionResult deductedStocks) {
        this.payment = payment;
        this.pgToken = pgToken;
        this.orderItems = orderItems;
        this.deductedStocks = deductedStocks;
    }
}