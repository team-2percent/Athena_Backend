package goorm.athena.domain.bankaccount.service;

import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.bankaccount.mapper.BankAccountMapper;
import goorm.athena.domain.bankaccount.repository.BankAccountRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankAccountCommandService {
    private final BankAccountRepository bankAccountRepository;
    private final UserQueryService userQueryService;
    private final BankAccountQueryService bankAccountQueryService;
    private final BankAccountMapper bankAccountMapper;

    @Transactional
    public void createBankAccount(Long userId, BankAccountCreateRequest request){
        User user = userQueryService.getUser(userId);

        boolean isDefault = !hasPrimaryDeliveryInfo(userId);

        BankAccount info = bankAccountMapper.toEntity(
                user,
                request,
                isDefault
        );

        bankAccountRepository.save(info);
    }

    @Transactional
    public void changeAccountState(Long userId, Long bankAccountId){
        BankAccount previousBankAccount = bankAccountQueryService.getPrimaryAccount(userId);
        BankAccount newBankAccount = bankAccountQueryService.getBankAccount(bankAccountId);

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

    @Transactional
    public void deleteBankAccount(Long userId, Long bankAccountId){

        BankAccount bankAccount = bankAccountQueryService.getBankAccount(bankAccountId);

        if(!bankAccount.getUser().getId().equals(userId)){
            throw new CustomException(ErrorCode.INACCURATE_BANK_ACCOUNT);
        }

        if(bankAccount.isDefault()){
            throw new CustomException(ErrorCode.BASIC_ACCOUNT_NOT_DELETED);
        }


        bankAccountRepository.delete(bankAccount);
    }

    public boolean hasPrimaryDeliveryInfo(Long userId) {
        return bankAccountRepository.existsByUserIdAndIsDefaultTrue(userId);
    }
}
