package goorm.athena.domain.bankaccount.service;

import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.mapper.BankAccountMapper;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankAccountQueryService {
    private final BankAccountRepository bankAccountRepository;
    private final UserService userService;

    public List<BankAccountGetResponse> getBankAccounts(Long userId){
        User user = userService.getUser(userId);
        List<BankAccount> bankAccount = bankAccountRepository.findByUser(user);
        return bankAccountRepository.findByUser(user).stream()
                .map(BankAccountMapper::toGetResponse)
                .collect(Collectors.toList());
    }

    public BankAccount getBankAccount(Long bankAccountId){
        return bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new CustomException(ErrorCode.BANK_ACCOUNT_NOT_FOUND));
    }

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
