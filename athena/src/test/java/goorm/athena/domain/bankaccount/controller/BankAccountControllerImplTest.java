package goorm.athena.domain.bankaccount.controller;

import goorm.athena.domain.bankaccount.BankAccountControllerIntegrationTestSupport;
import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountCreateResponse;
import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class BankAccountControllerImplTest extends BankAccountControllerIntegrationTestSupport {

    @DisplayName("로그인 한 사용자가 자신의 계좌 정보를 생성한다.")
    @Test
    void createBankAccount() {
        // given
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        BankAccountCreateRequest request = new BankAccountCreateRequest("123", "123", "123");

        // when
        ResponseEntity<BankAccountCreateResponse> response = controller.createBankAccount(loginUserRequest, request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(bankAccountService).createBankAccount(loginUserRequest.userId(), request);
    }

    @DisplayName("유저의 기존에 저장된 계좌 정보와 선택한 계좌 정보의 상태를 변경한다.")
    @Test
    void changeAccountState() {
        // given
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        Long bankAccountId = 1L;

        // when
        ResponseEntity<Void> response = controller.changeAccountState(loginUserRequest, bankAccountId);

        // then
        assertEquals(204, response.getStatusCodeValue());
        verify(bankAccountService).changeAccountState(loginUserRequest.userId(),bankAccountId);
    }

    @Test
    void deleteBankAccount() {
        // given
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        Long id = 1L;

        // when
        ResponseEntity<Void> response = controller.deleteBankAccount(loginUserRequest, id);

        // then
        assertEquals(204, response.getStatusCodeValue());
        verify(bankAccountService).deleteBankAccount(loginUserRequest.userId(), id);
    }

    @DisplayName("로그인 한 사용자가 자신의 선택한 계좌 정보를 삭제한다.")
    @Test
    void getBankAccount() {
        // given
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);

        // when
        ResponseEntity<List<BankAccountGetResponse>> response = controller.getBankAccount(loginUserRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(bankAccountService).getBankAccounts(loginUserRequest.userId());
    }
}