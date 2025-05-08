package goorm.athena.domain.user.dto.response;

public record RefreshTokenResponse(
        Long userId,
        String accessToken,
        String refreshToken
) {
}
