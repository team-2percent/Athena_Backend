package goorm.athena.domain.bankaccount.service;

import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountCreateResponse;
import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.mapper.BankAccountMapper;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final UserService userService;

    @Transactional
    public BankAccountCreateResponse createBankAccount(Long userId, BankAccountCreateRequest request){
        User user = userService.getUser(userId);

        boolean isDefault;
        try{
            getPrimaryAccount(userId);
            isDefault = false;
        } catch (CustomException e){
            if(e.getErrorCode() == ErrorCode.BANK_ACCOUNT_NOT_FOUND) {
                isDefault = true;
            } else {
                throw e;
            }
        }

        BankAccount bankAccount = BankAccountMapper.toEntity(user, request, isDefault);
        BankAccount saveAccount = bankAccountRepository.save(bankAccount);

        return BankAccountMapper.toCreateResponse(saveAccount);
    }

    public void changeAccountState(Long userId, Long bankAccountId){
        BankAccount previousBankAccount = getPrimaryAccount(userId);
        BankAccount newBankAccount = getBankAccount(bankAccountId);

        if(!newBankAccount.getUser().getId().equals(userId)){
            throw new CustomException(ErrorCode.INACCURATE_BANK_ACCOUNT);
        }

        if(previousBankAccount.getId().equals(bankAccountId)){
            throw new CustomException(ErrorCode.SAME_ACCOUNT_STATUS);
        }

        previousBankAccount.unsetAsDefault();
        newBankAccount.setAsDefault();

        bankAccountRepository.saveAll(List.of(previousBankAccount, newBankAccount));

    }

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

    @Transactional
    public void deleteBankAccount(Long userId, Long bankAccountId){

        BankAccount bankAccount = getBankAccount(bankAccountId);

        if(!bankAccount.getUser().getId().equals(userId)){
            throw new CustomException(ErrorCode.INACCURATE_BANK_ACCOUNT);
        }

        if(bankAccount.isDefault()){
            throw new CustomException(ErrorCode.BASIC_ACCOUNT_NOT_DELETED);
        }


        bankAccountRepository.delete(bankAccount);
    }
}
