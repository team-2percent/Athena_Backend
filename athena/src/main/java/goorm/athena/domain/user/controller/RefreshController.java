package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.dto.response.UserLoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Refresh", description = "리프레시 토큰 관련 API")
@RequestMapping("/api/refreshToken")
public interface RefreshController {

    @Operation(summary = "새로운 AccessToken과 RefreshToken 발급",
            description = "유효한 RefreshToken을 제공하면 새로운 AccessToken과 RefreshToken을 발급합니다. "
                    + "발급된 RefreshToken은 쿠키로 반환됩니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "AccessToken과 RefreshToken 발급 성공"),
                    @ApiResponse(responseCode = "400", description = "RefreshToken이 유효하지 않음"),
                    @ApiResponse(responseCode = "404", description = "사용자 정보 없음")
            })

    @PostMapping
    public ResponseEntity<RefreshTokenResponse> requestRefresh(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response);

    @Operation(summary = "액세스 토큰을 재발급합니다.")
    @ApiResponse(responseCode = "200", description = "액세스 토큰 재발급 성공")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response);
}
