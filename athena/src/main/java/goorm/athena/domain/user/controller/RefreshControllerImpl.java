package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.JwtTokenizer;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/refreshToken")
public class RefreshControllerImpl implements RefreshController{
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenizer jwtTokenizer;

    @Override
    @PostMapping("/ReissueRefresh")
    public ResponseEntity<RefreshTokenResponse> requestRefresh(@CookieValue("refreshToken") String refreshToken,
                                                               @RequestHeader("Authorization") String header,
                                                               HttpServletResponse response){
        String accessToken = jwtTokenizer.extractBearerToken(header);

        RefreshTokenResponse refreshResponse = refreshTokenService.reissueToken(accessToken, refreshToken, response);
        return ResponseEntity.ok(refreshResponse);
    }
}
