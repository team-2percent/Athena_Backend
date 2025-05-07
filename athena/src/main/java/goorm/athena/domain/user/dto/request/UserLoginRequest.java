package goorm.athena.domain.user.dto.request;

public record UserLoginRequest(
        String email,
        String password
) { }
