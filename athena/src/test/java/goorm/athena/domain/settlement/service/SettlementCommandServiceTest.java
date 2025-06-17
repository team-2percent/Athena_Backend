package goorm.athena.domain.settlement.service;

import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.settlement.SettlementIntegrationTestSupport;

import goorm.athena.domain.settlement.entity.Settlement;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SettlementCommandServiceTest extends SettlementIntegrationTestSupport {


    @Test
    @DisplayName("" +
            "시나리오 : 4월~6월 사이의 주문과 결제 데이터 이력이 있는 상태에서 6월 1~30일 사이에 정산 실행" +
            "원하는 결과 : 6월 1~30일 사이를 입력후 정산 요청을 하는 경우 -> " +
            "아래 상황에 대해서 프로젝트 id(1,2,7,8,9)에 대한 정산이 이루어져야 한다" +
            "1,2 : 후원 완료된 프로젝트(4월 프로젝트) - 정산 대상" +
            "3 : 프로젝트 후원 주문 취소(결제 미완료)" +
            "4,5 : 프로젝트 아직 후원 진행중 (프로젝트 상태 ACTIVE)" +
            "6 : 후원 취소 된 프로젝트(프로젝트 상태 CANCELLED)" +
            "7,8,9 : 5월 결제 - 정산 대상" +
            "10 : 6월에 진행된 정산 프로젝트 제외 대상" +

            "정산 대상 조건 : " +
            "1.프로젝트 종료일이 정산 기준일 이전이어야 함" +
            "2.프로젝트 상태가(status) 완료(COMPLETED) 여야함, " +
            "3.프로젝트는 관리자 승인이 허가(APPROVED)된 상태여야함 " +
            "4.프로젝트의 목표 달성 금액 이상을 후원받아야함," +
            "5.주문의 정산 처리상태(isSettled)가 아직 처리 되지 않아야함(false) " +
            "6.결제 상태가(status) 성공적으로 완료된 상태여야함(APPROVED)" +
            "정산 요청에서 입력을 ex)6월 1일로 한 경우 정산 범위는 5월1~ 5월30일이 된다")
    void executeMonthlySettlement_basedOnDataSql() {
        // given
        LocalDate settleDate = LocalDate.of(2025, 6, 30);

        // when
        settlementCommandService.executeMonthlySettlement(settleDate);

        // then
        List<Settlement> settlements = settlementRepository.findAll();
        assertThat(settlements).isNotEmpty();

        List<Long> settledProjectIds = settlements.stream()
                .map(s -> s.getProject().getId())
                .toList();

        assertThat(settledProjectIds).containsExactlyInAnyOrder(1L,2L,7L,8L,9L);

        // 해당 주문들이 정산되었는지 확인
        List<Order> settledOrders = orderRepository.findAll().stream()
                .filter(Order::isSettled)
                .toList();

        assertThat(settledOrders).isNotEmpty();
        assertThat(settledOrders).allMatch(Order::isSettled);
    }

}