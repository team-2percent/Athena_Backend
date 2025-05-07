package goorm.athena.domain.user.dto.response;

public record UserLoginResponse(
        String accessToken,
        String refreshToken,

        Long userId
) {
}
