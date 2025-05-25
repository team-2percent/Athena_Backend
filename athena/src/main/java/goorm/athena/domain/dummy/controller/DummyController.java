package goorm.athena.domain.dummy.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "~ 더미 데이터", description = "더미 유저 생성 API")
public interface DummyController {

    @Operation(summary = "더미 유저 생성", description = "입력된 숫자만큼 유저/계좌/배송지 더미 데이터를 생성합니다.")
    @PostMapping("/api/dev/dummy-users")
    ResponseEntity<String> createDummyUsers(@RequestParam(defaultValue = "10") int count);
}