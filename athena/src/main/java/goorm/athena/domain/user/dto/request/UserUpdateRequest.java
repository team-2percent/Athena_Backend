package goorm.athena.domain.user.dto.request;

public record UserUpdateRequest(
        Long id,
        String email,
        String password,
        String nickname
) { }
