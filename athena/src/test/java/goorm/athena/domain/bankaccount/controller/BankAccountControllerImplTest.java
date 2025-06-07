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
import org.springframework.http.HttpStatus;
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<BankAccount> all = bankAccountRepository.findAll();
        assertThat(oldSize+1).isEqualTo(all.size());
        assertThat("123").isEqualTo(all.get(all.size()-1).getAccountHolder());
    }

    @Transactional
    @DisplayName("유저의 기존에 저장된 계좌 정보와 선택한 계좌 정보의 상태를 변경한다.")
    @Test
    void changeAccountState() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);


        // 더미 데이터 기준이면 FALSE, 아니면 true
        BankAccount oldBankAccount = setupBankAccount(user, "123", "123", "123", false);
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
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

        int size = bankAccountRepository.findAllByUserId(user.getId()).size();

        // when
        ResponseEntity<List<BankAccountGetResponse>> response = controller.getBankAccount(loginUserRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get(size-1).accountHolder()).isEqualTo("124");
    }
}
