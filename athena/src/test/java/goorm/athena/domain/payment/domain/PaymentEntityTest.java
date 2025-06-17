package goorm.athena.domain.payment.domain;

import goorm.athena.domain.payment.PaymentIntegrationTestSupport;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.payment.entity.Payment;
import goorm.athena.domain.payment.entity.Status;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

public class PaymentEntityTest  extends PaymentIntegrationTestSupport {


    @Test
    @DisplayName("사용자가 결제 버튼을 눌렀을때 결제 요청한 사용자의 정보와 결제 정보에 맞게 결제 데이터를 생성한다")
    void createPayment_success() {
        // given
        User user = TestEntityFactory.createUser("test@email.com", "pwd1234", "닉네임", null, null);
        Order order = TestEntityFactory.createOrder(user, null, null, null);
        String tid = "TID_123";
        Long amount = 10000L;

        // when
        Payment payment = Payment.create(order, user, tid, amount);

        // then
        assertThat(payment.getOrder()).isEqualTo(order);
        assertThat(payment.getUser()).isEqualTo(user);
        assertThat(payment.getTid()).isEqualTo(tid);
        assertThat(payment.getAmountTotal()).isEqualTo(amount);
        assertThat(payment.getStatus()).isEqualTo(Status.PENDING);
        assertThat(payment.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("결제 승인에서 approve() 호출 시 결제 상태가 APPROVED로 바뀌고 요청한 pgToken과 현재 시간으로 승인 시간도 설정되어야 한다" +
            "(사용자가 카카오페이 결제 승인 버튼을 눌러 결제가 성공적으로 완료된 경우)")
    void approvePayment_success() {
        // given
        User user = TestEntityFactory.createUser("test@email.com", "pwd1234", "닉네임", null, null);
        Order order = TestEntityFactory.createOrder(user, null, null, null);
        Payment payment = Payment.create(order, user, "TID_456", 20000L);

        String pgToken = "PG_ABC123";
        LocalDateTime before = LocalDateTime.now();

        // when
        payment.approve(pgToken);
        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(payment.getStatus()).isEqualTo(Status.APPROVED);
        assertThat(payment.getPgToken()).isEqualTo(pgToken);
        assertThat(payment.getApprovedAt()).isAfterOrEqualTo(before);
        assertThat(payment.getApprovedAt()).isBeforeOrEqualTo(after);
    }

    @Test
    @DisplayName("결제 요청시 상태가 이미 승인(APPROVED)상태인 결제에 approve() 호출 시 '이미 결제가 완료된 주문입니다' 발생" +
            "(결제 완료 상태인 데이터를 다시 승인 방지)")
    void approvePayment_alreadyApproved_thenThrowException() {
        // given
        User user = TestEntityFactory.createUser("test@email.com", "pwd1234", "닉네임", null, null);
        Order order = TestEntityFactory.createOrder(user, null, null, null);
        Payment payment = Payment.create(order, user, "TID_XYZ", 30000L);

        // 결제 완료 데이터 이미 존재함
        payment.approve("PG_FIRST");

        // when & then (결제 재시도)
        assertThatThrownBy(() -> payment.approve("PG_SECOND"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.ALREADY_PAYMENT_COMPLETED.getErrorMessage());
    }
}
