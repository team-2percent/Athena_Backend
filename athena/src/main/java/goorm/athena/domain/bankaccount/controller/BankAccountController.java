package goorm.athena.domain.bankaccount.controller;

import goorm.athena.domain.bankaccount.dto.req.BankAccountCreateRequest;
import goorm.athena.domain.bankaccount.dto.res.BankAccountCreateResponse;
import goorm.athena.domain.bankaccount.dto.res.BankAccountGetResponse;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "BankAccount", description = "사용자 계좌 관련 API")
@RequestMapping("/api/bankAccount")
public interface BankAccountController {
    @Operation(summary = "사용자 계좌 추가 API", description = "입력된 정보로 로그인 한 사용자의 계좌 정보를 생성합나다.")
    @ApiResponse(responseCode = "200", description = "사용자 계좌 추가 생성 성공")
    @PostMapping
    public ResponseEntity<BankAccountCreateResponse> createBankAccount(@Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
                                                                       BankAccountCreateRequest request);

    @Operation(summary = "사용자 계좌 조회 API", description = "로그인 한 사용자의 계좌 정보들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "사용자 계좌 조회 성공")
    @GetMapping
    public ResponseEntity<List<BankAccountGetResponse>> getBankAccount(@Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest);
}
