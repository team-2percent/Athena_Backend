package goorm.athena.domain.settlement.controller;

import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Settlement", description = "정산 관련 API")
@RequestMapping("/api/admin/settlements")
public interface SettlementController {

    @Operation(
            summary = "정산 실행 (매월 1일 기준)",
            description = "사용자가 입력한 날짜에 해당하는 달의 1일을 기준으로 정산을 실행합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정산 실행 완료"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    @PostMapping("/execute")
    ResponseEntity<String> executeMonthlySettlement(
            @Parameter(description = "기준 날짜 (이 날짜의 달의 1일이 정산 기준이 됨)", example = "2025-05-14")
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}