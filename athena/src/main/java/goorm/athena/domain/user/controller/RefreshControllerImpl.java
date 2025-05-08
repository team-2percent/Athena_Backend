package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/refreshToken")
public class RefreshControllerImpl implements RefreshController{
    private final RefreshTokenService refreshTokenService;

    @Override
    @PostMapping("/ReissueRefresh")
    public ResponseEntity<RefreshTokenResponse> requestRefresh(@CookieValue("refreshToken") String refreshToken,
                                                               @RequestBody String accessToken,
                                                               HttpServletResponse response){

        RefreshTokenResponse refreshResponse = refreshTokenService.reissueToken(accessToken, refreshToken, response);
        return ResponseEntity.ok(refreshResponse);
    }
}
