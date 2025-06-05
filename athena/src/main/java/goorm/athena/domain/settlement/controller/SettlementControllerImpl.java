package goorm.athena.domain.settlement.controller;

import goorm.athena.domain.settlement.service.SettlementService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settlement")
public class SettlementControllerImpl implements SettlementController{

    private final SettlementService settlementService;

    @PostMapping("/execute")
    public ResponseEntity<String> executeMonthlySettlement(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate firstDayOfThisMonth = date.withDayOfMonth(1);

        settlementService.executeMonthlySettlement(firstDayOfThisMonth);

        return ResponseEntity.ok(" 정산 실행 완료 (기준일: " + firstDayOfThisMonth + ")");
    }
}
