package goorm.athena.domain.settlementhistory.mapper;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.settlement.entity.Settlement;
import goorm.athena.domain.settlementhistory.entity.SettlementHistory;

public class SettlementHistoryMapper {

    public static SettlementHistory toEntity(Settlement settlement, Order order, long totalPrice, int fee, long amount) {
        return SettlementHistory.builder()
                .settlement(settlement)
                .order(order)
                .totalPrice(totalPrice)
                .fee(fee)
                .amount(amount)
                .build();
    }
}