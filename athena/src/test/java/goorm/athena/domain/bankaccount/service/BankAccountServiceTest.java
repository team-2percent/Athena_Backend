package goorm.athena.domain.bankaccount.service;

import goorm.athena.domain.bankaccount.BankAccountIntegrationTestSupport;
import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BankAccountServiceTest extends BankAccountIntegrationTestSupport {

    @DisplayName("유저가 존재하지 않는 계좌 정보를 조회하면 에러를 리턴한다.")
    @Test
    void getBankAccountById_Error() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);
        BankAccount bankAccount = setupBankAccount(user, "123", "123", "123", true);
        bankAccountRepository.save(bankAccount);

        // when & then
        assertThatThrownBy(() -> bankAccountQueryService.getBankAccount(100000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BANK_ACCOUNT_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 계좌 정보를 저장했다면 계좌 정보의 ID로 해당 정보를 조회한다.")
    @Test
    void getBankAccountById() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);
        BankAccount bankAccount = setupBankAccount(user, "123", "123", "123", true);
        bankAccountRepository.save(bankAccount);

        // when
        BankAccount found = bankAccountQueryService.getBankAccount(bankAccount.getId());

        // then
        assertThat(found.getId()).isEqualTo(bankAccount.getId());
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
        assertThat(found.getAccountHolder()).isEqualTo("123");
    }

    @DisplayName("유저가 자신의 계좌 정보를 입력해 저장한다.")
    @Test
    void createBankAccount_Primary() {
        // given
        User user = userRepository.findById(2L).get();

        BankAccountCreateRequest request = new BankAccountCreateRequest("123", "123", "123");

        // when
        bankAccountCommandService.createBankAccount(user.getId(), request);

        // then
        List<BankAccount> infos = bankAccountRepository.findAllByUserId(user.getId());
        assertThat(infos.getLast().isDefault()).isTrue();
        assertThat(infos.getFirst().isDefault()).isFalse();
    }

    @DisplayName("기본 계좌 정보를 조회할 시 해당 정보가 존재하지 않을 경우 에러를 리턴한다.")
    @Test
    void getPrimaryDeliveryInfo_ThrowsDeliveryNotFound() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("user_no_bankAccount", "abc", "abc", imageGroup);
        userRepository.save(user);

        // 기본 계좌 정보 없음 상태

        // when & then
        assertThatThrownBy(() -> bankAccountQueryService.getPrimaryAccount(100000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BANK_ACCOUNT_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 이미 기존 계좌 정보를 저장했었다면 현재 계좌 정보는 일반 계좌 정보로 저장한다.")
    @Test
    void createBankAccount_Normal() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);
        BankAccount bankAccount = setupBankAccount(user, "124", "124", "124", true);
        bankAccountRepository.save(bankAccount);

        BankAccountCreateRequest request = new BankAccountCreateRequest("123", "123", "123");

        int size = bankAccountRepository.findAllByUserId(user.getId()).size();

        // when
        bankAccountCommandService.createBankAccount(user.getId(), request);

        // then
        List<BankAccount> infos = bankAccountRepository.findAllByUserId(user.getId());
        assertThat(infos).hasSize(size+1);
        assertThat(infos.get(size).isDefault()).isFalse();
    }

    @DisplayName("유저가 계좌 정보 상태를 변경할 때 다른 유저의 계좌 정보를 변경하면 에러를 리턴한다.")
    @Test
    void changeBankAccountState_NotMyUser() {
        // given
        User user = userRepository.findById(1L).get();
        User user2 = userRepository.findById(3L).get();

        BankAccount newBankAccount = bankAccountRepository.findById(user2.getId()).get();

        // when & then
        assertThatThrownBy(() -> bankAccountCommandService.changeAccountState(user.getId(), newBankAccount.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INACCURATE_BANK_ACCOUNT.getErrorMessage());
    }

    @DisplayName("유저가 기본 계좌를 기본 계좌 상태로 변경하려 하면 에러를 리턴한다.")
    @Test
    void changeBankAccountState_ALREADY() {
        // given
        User user = userRepository.findById(1L).get();

        BankAccount primaryAccount = bankAccountQueryService.getPrimaryAccount(user.getId());

        // when & then
        assertThatThrownBy(() -> bankAccountCommandService.changeAccountState(user.getId(), primaryAccount.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.SAME_ACCOUNT_STATUS.getErrorMessage());
    }

    @DisplayName("현재의 계좌 정보를 기본 계좌 정보로 변경하고, 이전 계좌 정보는 일반 계좌 정보로 변경한다.")
    @Test
    void changeBankAccountState() {
        // given
        User user = userRepository.findById(3L).get();

        // when
        bankAccountCommandService.changeAccountState(user.getId(), 31L);

        BankAccount updatedOld = bankAccountRepository.findById(3L).get();
        BankAccount updatedNew = bankAccountRepository.findById(31L).get();

        // then
        assertThat(updatedOld.isDefault()).isFalse();
        assertThat(updatedNew.isDefault()).isTrue();
    }

    @DisplayName("유저가 다른 사람의 계좌 정보를 삭제하고자 하면 접근 거부 에러를 리턴한다.")
    @Test
    void deleteBankAccountInfo_NotMyUser() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);
        BankAccount bankAccount = setupBankAccount(user, "!23", "123", "123", true);
        bankAccountRepository.save(bankAccount);

        // when & then
        assertThatThrownBy(() -> bankAccountCommandService.deleteBankAccount(99L, bankAccount.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INACCURATE_BANK_ACCOUNT.getErrorMessage());
    }

    @DisplayName("유저가 자신의 기본 계좌 정보를 삭제하고자 하면 접근 거부 에러를 리턴한다.")
    @Test
    void deleteBankAccountInfo_Primary() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);
        BankAccount bankAccount = setupBankAccount(user, "!23", "123", "123", true);
        bankAccountRepository.save(bankAccount);

        // when & then
        assertThatThrownBy(() -> bankAccountCommandService.deleteBankAccount(user.getId(), bankAccount.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BASIC_ACCOUNT_NOT_DELETED.getErrorMessage());
    }

    @DisplayName("유저가 등록했던 자신의 일반 계좌 정보 중 하나를 삭제한다.")
    @Test
    void deleteBankAccountInfo_Normal() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        BankAccount bankAccount = setupBankAccount(user, "!23", "123", "123", true);
        BankAccount bankAccount2 = setupBankAccount(user, "!234", "124", "1243", false);
        bankAccountRepository.saveAll(List.of(bankAccount, bankAccount2));

        // when
        bankAccountCommandService.deleteBankAccount(user.getId(), bankAccount2.getId());

        // then
        boolean exists = bankAccountRepository.findById(bankAccount2.getId()).isPresent();
        assertThat(exists).isFalse();
    }

    @DisplayName("유저가 자신이 등록한 계좌 정보들을 조회한다.")
    @Test
    void getMyDeliveryInfo() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);

        BankAccount bankAccount = setupBankAccount(user, "!23", "123", "123", false);
        BankAccount bankAccount2 = setupBankAccount(user, "!234", "124", "1243", false);
        bankAccountRepository.saveAll(List.of(bankAccount, bankAccount2));

        int size = bankAccountRepository.findAllByUserId(user.getId()).size();

        // when
        List<BankAccountGetResponse> responses = bankAccountQueryService.getBankAccounts(user.getId());

        // then
        assertThat(responses).hasSize(size);
        assertThat(responses.get(size-2).bankAccount()).isEqualTo("!23");
        assertThat(responses.get(size-1).bankAccount()).isEqualTo("!234");
    }

    @DisplayName("유저가 자신이 등록한 기본 계좌 정보를 조회한다.")
    @Test
    void getPrimaryDeliveryInfo() {
        // given
        User user = userRepository.findById(1L).get();

        BankAccount bankAccount = setupBankAccount(user, "!23", "123", "123", false);
        BankAccount bankAccount2 = setupBankAccount(user, "!234", "124", "1243", false);
        bankAccountRepository.saveAll(List.of(bankAccount, bankAccount2));

        // when
        BankAccount response = bankAccountQueryService.getPrimaryAccount(user.getId());

        // then
        assertThat(response.isDefault()).isTrue();
    }



    /*
    @DisplayName("로그인 한 유저가 없는 계좌 정보를 조회하면 에러를 리턴한다.")
    @Test
    void getAccount_Error(){
        // given
        System.out.println(userRepository.findAll().size()+"가가");


    }

    @DisplayName("로그인 한 유저의 특정 계좌 정보를 리턴한다.")
    @Test
    void getAccount(){
        System.out.println(userRepository.findAll().size()+"나나");


    }

     */
}