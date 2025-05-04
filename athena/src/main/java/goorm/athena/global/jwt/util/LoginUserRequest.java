package goorm.athena.global.jwt.util;

public record LoginUserRequest(
        String email,
        Long memberId,
        String roles
) { }
