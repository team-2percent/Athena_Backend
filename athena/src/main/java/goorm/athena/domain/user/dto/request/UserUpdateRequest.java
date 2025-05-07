package goorm.athena.domain.user.dto.request;

public record UserUpdateRequest(
        String email,
        String password,
        String nickname
) { }
