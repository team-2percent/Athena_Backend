package goorm.athena.domain.settlement.scheduler;

import goorm.athena.domain.settlement.service.SettlementCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.ZoneId;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {
    private final SettlementCommandService settlementCommandService;

    @Scheduled(cron = "0 0 3 1 * ?") // 매월 1일 오전 3시
    public void autoSettlement() {
        try {
            settlementCommandService.executeMonthlySettlement(LocalDate.now(ZoneId.of("Asia/Seoul")));
            log.info(" 월 정산 스케줄러 정상 실행 완료");
        } catch (Exception e) {
            log.error(" 월 정산 스케줄러 실행 중 오류 발생", e);
        }
    }
}
