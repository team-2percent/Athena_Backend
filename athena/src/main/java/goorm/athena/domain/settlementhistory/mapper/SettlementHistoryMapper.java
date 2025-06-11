package goorm.athena.domain.settlementhistory.mapper;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.settlement.entity.Settlement;
import goorm.athena.domain.settlementhistory.entity.SettlementHistory;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SettlementHistoryMapper {

    @Mapping(target = "order", source = "order")
    SettlementHistory toEntity(Settlement settlement, Order order,
                               long totalPrice, long platformFee, long pgFee, long vat, long amount);
}




