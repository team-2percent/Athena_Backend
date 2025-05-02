package goorm.athena.domain.user.dto.request;

public record UserCreateRequest(
        String email,
        String password,
        String nickname
) {
}
