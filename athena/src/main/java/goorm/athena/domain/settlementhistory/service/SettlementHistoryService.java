package goorm.athena.domain.settlementhistory.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
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
        // 프로젝트에서 요금제 정보 가져오기
        Project project = settlement.getProject();
        PlatformPlan plan = project.getPlatformPlan();

        double platformRate = plan.getPlatformFeeRate();
        double pgRate = plan.getPgFeeRate();
        double vatRate = plan.getVatRate(); 

        List<SettlementHistory> histories = orders.stream()
                .map(order -> {
                    long totalPrice = order.getTotalPrice();

                    long platformFee = Math.round(totalPrice * platformRate);   // 플랫폼 수수료(과세 대상 아님)
                    long pgFee = Math.round(totalPrice * pgRate);               // PG사 수수료(과세 대상 아님)
                    long vat = Math.round(platformFee * vatRate);               // VAT는 플랫폼 수수료에만 적용
                    long amount = totalPrice - platformFee - pgFee - vat;       // 최종 정산금액

                    return toEntity(settlement, order, totalPrice, platformFee, pgFee, vat, amount);
                })
                .toList();

        settlementHistoryRepository.saveAll(histories);
    }
}