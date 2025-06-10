package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import goorm.athena.domain.user.service.RefreshTokenCommandService;
import goorm.athena.global.jwt.util.JwtTokenizer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/refreshToken")
public class RefreshControllerImpl implements RefreshController{
    private final RefreshTokenCommandService refreshTokenCommandService;
    private final JwtTokenizer jwtTokenizer;

    @Override
    @PostMapping("/ReissueRefresh")
    public ResponseEntity<RefreshTokenResponse> requestRefresh(@CookieValue("refreshToken") String refreshToken,
                                                               @RequestHeader("Authorization") String header,
                                                               HttpServletResponse response){
        String accessToken = jwtTokenizer.extractBearerToken(header);

        RefreshTokenResponse refreshResponse = refreshTokenCommandService.reissueToken(accessToken, refreshToken, response);
        return ResponseEntity.ok(refreshResponse);
    }
}
