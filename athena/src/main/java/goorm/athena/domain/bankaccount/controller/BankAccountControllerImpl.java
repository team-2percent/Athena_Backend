package goorm.athena.domain.bankaccount.controller;

import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountCreateResponse;
import goorm.athena.domain.bankaccount.service.BankAccountService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/bankAccount")
public class BankAccountControllerImpl implements BankAccountController{
    private final BankAccountService bankAccountService;

    @Override
    public ResponseEntity<BankAccountCreateResponse> createBankAccount(@CheckLogin LoginUserRequest loginUserRequest,
                                                                       @RequestBody BankAccountCreateRequest request){
        BankAccountCreateResponse response = bankAccountService.createBankAccount(loginUserRequest.userId(), request);
        return ResponseEntity.ok(response);
    }

}
