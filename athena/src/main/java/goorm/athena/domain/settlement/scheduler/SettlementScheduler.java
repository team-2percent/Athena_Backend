package goorm.athena.domain.settlement.scheduler;

import goorm.athena.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SettlementScheduler {
    private final SettlementService settlementService;

    @Scheduled(cron = "0 0 3 1 * ?") // 매월 1일 오전 3시
    public void autoSettlement() {
        settlementService.executeMonthlySettlement(LocalDate.now());
    }
}
