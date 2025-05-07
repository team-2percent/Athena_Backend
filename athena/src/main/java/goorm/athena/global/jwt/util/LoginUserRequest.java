package goorm.athena.global.jwt.util;

public record LoginUserRequest(
        String email,
        Long userId,
        String role
) { }
