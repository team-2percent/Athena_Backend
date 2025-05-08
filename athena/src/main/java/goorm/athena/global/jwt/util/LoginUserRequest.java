package goorm.athena.global.jwt.util;

public record LoginUserRequest(
        String nickname,
        Long userId,
        String role
) { }
