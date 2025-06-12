package goorm.athena.domain.bankaccount.service;

import goorm.athena.domain.bankaccount.BankAccountIntegrationTestSupport;
import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class BankAccountServiceTest extends BankAccountIntegrationTestSupport {

    @DisplayName("존재하지 않는 계좌 정보를 조회하면, 'BANK_ACCOUNT_NOT_FOUND' 에러를 리턴한다.")
    @Test
    void getBankAccountById_Error() {
        // given

        // when & then
        assertThatThrownBy(() -> bankAccountQueryService.getBankAccount(100000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BANK_ACCOUNT_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("13번 유저가 기본 계좌 정보를 저장했다면 기본 계좌 정보의 주인이 13번 유저가 맞는지 검증한다.")
    @Test
    void getBankAccountById() {
        // given
        User user = userRepository.findById(13L).get();

        // when
        BankAccount bankAccount = bankAccountQueryService.getPrimaryAccount(user.getId());

        // then
        assertThat(bankAccount.getId()).isEqualTo(13L);
        assertThat(bankAccount.getUser().getId()).isEqualTo(user.getId());
    }

    @DisplayName("8번 유저가 기본 계좌가 없는 상태에서 계좌를 생성했다면 신규 생성 계좌가 기본 계좌인지 검증한다.")
    @Test
    void createBankAccount_Primary() {
        // given
        User user = userRepository.findById(8L).get();

        BankAccountCreateRequest request = new BankAccountCreateRequest("123", "123", "123");

        // when
        bankAccountCommandService.createBankAccount(user.getId(), request);

        // then
        List<BankAccount> infos = bankAccountRepository.findAllByUserId(user.getId());
        assertThat(infos.getLast().isDefault()).isTrue();
        assertThat(infos.getFirst().isDefault()).isFalse();
    }

    @DisplayName("2번 유저의 ID로 기본 계좌 정보를 조회할 시 유저 ID로 저장된 기본 계좌가 존재하지 않으면 'BANK_ACCOUNT_NOT_FOUND' 에러를 리턴한다.")
    @Test
    void getPrimaryDeliveryInfo_ThrowsDeliveryNotFound() {
        // given
        User user = userRepository.findById(2L).get();

        // when & then
        assertThatThrownBy(() -> bankAccountQueryService.getPrimaryAccount(100000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BANK_ACCOUNT_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("27번 유저가 이미 기존 계좌를 저장했었다면 현재 생성한 계좌는 일반 계좌로 저장한다.")
    @Test
    void createBankAccount_Normal() {
        // given
        User user = userRepository.findById(27L).get();
        BankAccountCreateRequest request = new BankAccountCreateRequest("123", "123", "123");

        // when
        bankAccountCommandService.createBankAccount(user.getId(), request);

        // then
        List<BankAccount> infos = bankAccountRepository.findAllByUserId(user.getId());
        assertThat(infos.getLast().isDefault()).isFalse();
        assertThat(infos.getFirst().isDefault()).isTrue();
    }

    @DisplayName("유저가 계좌 정보 상태를 변경할 때 다른 유저의 계좌 정보를 변경하면 'INACCURATE_BANK_ACCOUNT' 에러를 리턴한다.")
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

    @DisplayName("1번 유저가 자신의 기본 계좌를 다시 기본 계좌 상태로 변경하려 하면 SAME_ACCOUNT_STATUS 에러를 리턴한다.")
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

    @DisplayName("3번 유저가 기본 계좌, 일반 계좌를 보유 중일 때 기본 계좌를 변경할려고 하면" +
            "변경할 일반 계좌를 기본 계좌로 변경하고, 이전 기본 계좌는 일반 계좌로 변경한다.")
    @Test
    void changeBankAccountState() {
        // given
        User user = userRepository.findById(3L).get();

        List<BankAccount> bankAccountList = bankAccountRepository.findAllByUserId(user.getId());

        // when
        bankAccountCommandService.changeAccountState(user.getId(), bankAccountList.get(1).getId());

        BankAccount updatedOld = bankAccountList.get(0);
        BankAccount updatedNew = bankAccountList.get(1);

        // then
        assertThat(updatedOld.isDefault()).isFalse();
        assertThat(updatedNew.isDefault()).isTrue();
    }

    @DisplayName("16번 유저가 다른 사람의 계좌 정보를 삭제하고자 하면 'INACCURATE_BANK_ACCOUNT' 에러를 리턴한다.")
    @Test
    void deleteBankAccountInfo_NotMyUser() {
        // given
        User user = userRepository.findById(16L).get();

        // when & then
        assertThatThrownBy(() -> bankAccountCommandService.deleteBankAccount(user.getId(), 1L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INACCURATE_BANK_ACCOUNT.getErrorMessage());
    }

    @DisplayName("16번 유저가 자신의 기본 계좌 정보를 삭제하고자 하면 'BASIC_ACCOUNT_NOT_DELETED' 에러를 리턴한다.")
    @Test
    void deleteBankAccountInfo_Primary() {
        // given
        User user = userRepository.findById(16L).get();
        BankAccount bankAccount = bankAccountQueryService.getPrimaryAccount(user.getId());

        // when & then
        assertThatThrownBy(() -> bankAccountCommandService.deleteBankAccount(user.getId(), bankAccount.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BASIC_ACCOUNT_NOT_DELETED.getErrorMessage());
    }

    @DisplayName("30번 유저가 기본 계좌와 일반 계좌를 생성한 후 선택한 일반 계좌를 삭제하면 정상적으로 삭제된다.")
    @Test
    void deleteBankAccountInfo_Normal() {
        // given
        User user = userRepository.findById(30L).get();
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

    @DisplayName("3번 유저가 일반 계좌를 두 개 생성한 뒤 자신의 계좌를 조회하면, 등록한 모든 계좌 정보가 조회된다.")
    @Test
    void getMyDeliveryInfo() {
        // given
        User user = userRepository.findById(3L).get();

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
        assertThat(responses.get(size-3).bankAccount()).isEqualTo("0000000025");
    }

    @DisplayName("1번 유저가 일반 계좌를 두 개 생성하고 기본 계좌를 조회하면, 등록한 기본 계좌만 조회한다.")
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

    @DisplayName("25번 유저가 없는 계좌를 조회하면 BANK_ACCOUNT_NOT_FOUND 에러를 리턴한다.")
    @Test
    void getAccount_Error(){
        // given
        User user = userRepository.findById(25L).get();

        BankAccount bankAccount = setupBankAccount(user, "!23", "123", "123", true);
        BankAccount bankAccount2 = setupBankAccount(user, "!234", "124", "1243", false);
        bankAccountRepository.saveAll(List.of(bankAccount, bankAccount2));

        // when
        assertThatThrownBy(() -> bankAccountQueryService.getAccount(user.getId(), 3000000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BANK_ACCOUNT_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("25번 유저가 조회하는 계좌 정보가 자신이 등록한 계좌라면, 특정 계좌의 정보를 리턴한다.")
    @Test
    void getAccount(){
        // given
        User user = userRepository.findById(25L).get();

        // when
        BankAccount response = bankAccountQueryService.getAccount(user.getId(), 25L);

        // then
        assertThat(response.getAccountNumber()).isEqualTo("0000000020");
        assertThat(response.isDefault()).isTrue();
    }
}