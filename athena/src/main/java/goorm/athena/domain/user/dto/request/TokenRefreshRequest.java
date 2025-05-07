package goorm.athena.domain.user.dto.request;

public record TokenRefreshRequest(
        String refreshToken
) {
}
