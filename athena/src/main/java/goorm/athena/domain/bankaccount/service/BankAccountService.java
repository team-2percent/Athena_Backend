package goorm.athena.domain.bankaccount.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;

    public BankAccount getPrimaryAccount(Long userId) {
        return bankAccountRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
    }

    // 사용자에 대한 특정 계좌 객체 return
    public BankAccount getAccount(Long userId, Long bankAccountId) {
        List<BankAccount> bankAccounts = bankAccountRepository.findAllByUserId(userId);
        for (BankAccount bankAccount : bankAccounts) {
            if (bankAccountId.equals(bankAccount.getId())) {
                return bankAccount;
            }
        }
        throw new CustomException(ErrorCode.BANK_ACCOUNT_NOT_FOUND);
    }
}
