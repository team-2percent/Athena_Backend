package goorm.athena.domain.bankaccount.controller;

import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountCreateResponse;
import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.domain.bankaccount.service.BankAccountCommandService;
import goorm.athena.domain.bankaccount.service.BankAccountQueryService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bankAccount")
public class BankAccountControllerImpl implements BankAccountController{
    private final BankAccountQueryService bankAccountQueryService;
    private final BankAccountCommandService bankAccountCommandService;

    @Override
    public ResponseEntity<BankAccountCreateResponse> createBankAccount(@CheckLogin LoginUserRequest loginUserRequest,
                                                                       @RequestBody BankAccountCreateRequest request){
        BankAccountCreateResponse response = bankAccountCommandService.createBankAccount(loginUserRequest.userId(), request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<BankAccountGetResponse>> getBankAccount(@CheckLogin LoginUserRequest loginUserRequest){
        List<BankAccountGetResponse> response = bankAccountQueryService.getBankAccounts(loginUserRequest.userId());
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> changeAccountState(@CheckLogin LoginUserRequest loginUserRequest,
                                                   @RequestParam Long bankAccountId){
        bankAccountCommandService.changeAccountState(loginUserRequest.userId(), bankAccountId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> deleteBankAccount(@CheckLogin LoginUserRequest loginUserRequest,
                                                  @RequestParam Long bankAccountId){
        bankAccountCommandService.deleteBankAccount(loginUserRequest.userId(), bankAccountId);
        return ResponseEntity.noContent().build();
    }

}
