package goorm.athena.domain.settlement.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.service.BankAccountQueryService;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.repository.OrderRepository;
import goorm.athena.domain.payment.service.PaymentQueryService;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.service.ProjectQueryService;
import goorm.athena.domain.settlement.dto.res.SettlementSummaryResponse;
import goorm.athena.domain.settlement.entity.Settlement;
import goorm.athena.domain.settlement.mapper.SettlementMapper;
import goorm.athena.domain.settlement.repository.SettlementRepository;
import goorm.athena.domain.settlementhistory.service.SettlementHistoryCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementCommandService {

    private final OrderRepository orderRepository;
    private final ProjectQueryService projectQueryService;
    private final SettlementRepository settlementRepository;
    private final BankAccountQueryService bankAccountQueryService;
    private final SettlementHistoryCommandService historyService;
    private final PaymentQueryService paymentQueryService;
    private final SettlementMapper settlementMapper;

    @Transactional
    public void executeMonthlySettlement(LocalDate settleDate) {
        LocalDate end = YearMonth.from(settleDate.minusMonths(1)).atEndOfMonth();

        // 후원 성공한 정산 대상 프로젝트 조회
        List<Project> projects = projectQueryService.getEligibleProjects(end);
        List<Long> projectIds = projects.stream()
                .map(Project::getId)
                .toList();
        log.info("정산 대상 프로젝트 수: {} | 프로젝트 ID: {}", projects.size(), projectIds);
        if (projects.isEmpty()) {
            log.info("정산 대상 프로젝트 없음. 종료.");
            return;
        }

        // 전체 프로젝트의 주문을 한 번에 조회
        // ex) 프로젝트 목록에서 후원 기간 처음과 끝 기간중 최소 최대를 필터 후 주문 데이터를 가져옴
        List<Order> allOrders = paymentQueryService.getUnsettledOrdersByProjects(projects);
        log.info("전체 미정산 주문 수: {}", allOrders.size());

        // 프로젝트별 주문을 그룹핑
        Map<Long, List<Order>> orderMap = allOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getProject().getId()));

        for (Project project : projects) {
            List<Order> orders = orderMap.getOrDefault(project.getId(), List.of());
            log.info(" 프로젝트 ID={} | 미정산 주문 수={}", project.getId(), orders.size());
            if (orders.isEmpty()) continue;

            Settlement settlement = createSettlement(project, orders);
            settlementRepository.save(settlement);
            historyService.saveAll(settlement, orders);
            log.info("정산 저장 완료 - ProjectID={}, 금액={}, 건수={}",
                    project.getId(), settlement.getTotalSales(), orders.size());

            orders.forEach(Order::markAsSettled);
            orderRepository.saveAll(orders);
        }
    }

    private Settlement createSettlement(Project project, List<Order> orders) {
        long totalSales = orders.stream().mapToLong(Order::getTotalPrice).sum();
        int totalCount = orders.size();

        // 1. 프로젝트의 PlatformPlan 정보 가져오기
        PlatformPlan plan = project.getPlatformPlan();
        double platformRate = plan.getPlatformFeeRate();
        double pgRate = plan.getPgFeeRate();
        double vatRate = plan.getVatRate();

        // 2. 수수료 계산
        long platformFeeTotal = Math.round(totalSales * platformRate);
        long pgFeeTotal = Math.round(totalSales * pgRate);
        long vatTotal = Math.round(platformFeeTotal * vatRate);
        long payOutAmount = totalSales - platformFeeTotal - pgFeeTotal - vatTotal;

        // 3. 판매자 계좌 정보 가져오기
        BankAccount bankAccount = bankAccountQueryService.getPrimaryAccount(project.getSeller().getId());

        return settlementMapper.toEntity(
                project,
                bankAccount,
                totalCount,
                totalSales,
                platformFeeTotal,
                pgFeeTotal,
                vatTotal,
                payOutAmount
        );
    }
    
}