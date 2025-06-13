package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Refresh", description = "리프레시 토큰 관련 API")
@RequestMapping("/api/refreshToken")
public interface RefreshController {

    @Operation(summary = "새로운 AccessToken과 RefreshToken 발급",
            description = "유효한 RefreshToken을 제공하면 새로운 AccessToken과 RefreshToken을 발급합니다. <br>"
                    + "발급된 RefreshToken은 쿠키로 반환됩니다.<br>" +
                    "accessKey가 '만료됏거나, 만료되지 않았을 경우'에 각각 그대로 리턴, 재발급이 이루어지며 refreshKey가 만료됐을 경우 에러를 리턴합니다.(재로그인)<br>" +
                    "로그인 진행 시 발급되었던 accessToken을 상단 위의 Authorize 버튼에 입력하며," +
                    "이는 next.js에서 'Authorization' 헤더로 accessToken을 보낼 때와 같습니다.<br>" +
                    "refreshToken도 쿠키에 있던 값을 기준으로 발급되니 따로 입력값이 필요하지 않습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AccessToken과 RefreshToken 발급 성공"),
                    @ApiResponse(responseCode = "400", description = "RefreshToken이 유효하지 않음"),
                    @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
            })

    @PostMapping("/ReissueRefresh")
    public ResponseEntity<RefreshTokenResponse> requestRefresh(@Parameter(hidden = true) @CookieValue("refreshToken") String refreshToken,
                                                               @Parameter(hidden = true) @RequestHeader("Authorization") String header,
                                                               HttpServletResponse response);
}