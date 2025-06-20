package goorm.athena.domain.notification.controller;

import goorm.athena.domain.notification.dto.FcmLoginRequest;
import goorm.athena.domain.notification.entity.FcmToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "FCM", description = "FCM 알림 API")
@RequestMapping("/api/fcm")
public interface FcmController {

    @Operation(summary = "FCM 토큰 API", description = "로그인 된 사용자에 대해 FCM 토큰을 생성합니다.<br>" +
            "생성된 토큰을 통해서 특정 사용자가 알림을 받을 수 있습니다.<br>")
    @ApiResponse(responseCode = "200", description = "FCM 토큰 생성 성공",
            content = @Content(schema = @Schema(implementation = FcmToken.class)))
    @PostMapping("/register")
    ResponseEntity<Void> createToken(@RequestBody FcmLoginRequest fcmLoginRequest);

    /***
     * 알림 테스트용 API
     */
    @Operation(summary = "FCM 테스트 API", description = "FCM 부하 테스트 용 API 입니다.")
    @ApiResponse(responseCode = "200", description = "FCM 알림 전송 성공")
    @PostMapping("/test")
    ResponseEntity<Void> test(@RequestParam("token") String token);
}
