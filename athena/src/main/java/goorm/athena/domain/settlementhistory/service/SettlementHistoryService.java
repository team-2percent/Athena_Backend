package goorm.athena.domain.settlementhistory.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.settlement.entity.Settlement;
import goorm.athena.domain.settlementhistory.entity.SettlementHistory;
import goorm.athena.domain.settlementhistory.repository.SettlementHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static goorm.athena.domain.settlementhistory.mapper.SettlementHistoryMapper.toEntity;

@Service
@RequiredArgsConstructor
public class SettlementHistoryService {

    private final SettlementHistoryRepository settlementHistoryRepository;

    private static final double PLATFORM_FEE_RATE = 0.10;

    // 정산 데이터의 총 판매금액과 총수수료의 정보와 정산기록의 각 데이터의 수수료와 총금액의 합산이 안맞을 수가 있다.
    @Transactional
    public void saveAll(Settlement settlement, List<Order> orders) {
        List<SettlementHistory> histories = orders.stream()
                .map(order -> {
                    long totalPrice = order.getTotalPrice();
                    int fee = (int) (totalPrice * PLATFORM_FEE_RATE);
                    long amount = totalPrice - fee;
                    return toEntity(settlement, order, totalPrice, fee, amount);
                })
                .toList();

        settlementHistoryRepository.saveAll(histories);
    }
}