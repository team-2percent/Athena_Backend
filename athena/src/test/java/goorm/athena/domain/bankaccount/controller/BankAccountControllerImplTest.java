package goorm.athena.domain.bankaccount.controller;

import goorm.athena.domain.bankaccount.BankAccountControllerIntegrationTestSupport;
import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountCreateResponse;
import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.jwt.util.LoginUserRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BankAccountControllerImplTest extends BankAccountControllerIntegrationTestSupport {

    @DisplayName("로그인 한 사용자가 자신의 계좌 정보를 생성한다.")
    @Test
    void createBankAccount() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);
        BankAccountCreateRequest request = new BankAccountCreateRequest("123", "123", "123");

        long oldSize = bankAccountRepository.count();

        // when
        ResponseEntity<BankAccountCreateResponse> response = controller.createBankAccount(loginUserRequest, request);

        // then
        assertEquals(200, response.getStatusCodeValue());

        List<BankAccount> all = bankAccountRepository.findAll();
        assertEquals(oldSize+1, all.size());
        assertEquals("123", all.get(all.size()-1).getAccountHolder());
    }

    @Transactional
    @DisplayName("유저의 기존에 저장된 계좌 정보와 선택한 계좌 정보의 상태를 변경한다.")
    @Test
    void changeAccountState() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);


        BankAccount oldBankAccount = setupBankAccount(user, "123", "123", "123", true);
        BankAccount newBankAccount = setupBankAccount(user, "123", "123", "123", false);

        userRepository.save(user);
        bankAccountRepository.saveAll(List.of(oldBankAccount, newBankAccount));

        BankAccount oldPrimaryAccount = bankAccountService.getPrimaryAccount(user.getId());

        // when
        controller.changeAccountState(loginUserRequest, newBankAccount.getId());

        // then
        assertThat(newBankAccount.isDefault()).isTrue();
        assertThat(oldPrimaryAccount.isDefault()).isFalse();
    }

    @Transactional
    @DisplayName("로그인 한 사용자가 자신이 선택한 계좌 정보를 삭제한다.")
    @Test
    void deleteBankAccount() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);
        BankAccount bankAccount = setupBankAccount(user, "!23", "123", "123", false);
        BankAccount bankAccount2 = setupBankAccount(user, "!234", "124", "1243", false);
        bankAccountRepository.saveAll(List.of(bankAccount, bankAccount2));

        // when
        ResponseEntity<Void> response = controller.deleteBankAccount(loginUserRequest, bankAccount2.getId());

        // then
        assertEquals(204, response.getStatusCodeValue());
        boolean exists = bankAccountRepository.findById(bankAccount2.getId()).isPresent();
        assertThat(exists).isFalse();
    }

    @DisplayName("로그인 한 사용자가 자신의 선택한 계좌 정보를 삭제한다.")
    @Test
    void getBankAccount() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        BankAccount bankAccount = setupBankAccount(user, "!23", "123", "123", false);
        BankAccount bankAccount2 = setupBankAccount(user, "!234", "124", "1243", false);
        bankAccountRepository.saveAll(List.of(bankAccount, bankAccount2));

        // when
        ResponseEntity<List<BankAccountGetResponse>> response = controller.getBankAccount(loginUserRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(response.getBody().size(), 2);
        assertEquals(response.getBody().get(1).accountHolder(), "124");
    }
}