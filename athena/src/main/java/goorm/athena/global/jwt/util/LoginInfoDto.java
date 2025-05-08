package goorm.athena.global.jwt.util;

public record LoginInfoDto(
        Long userId,
        String nickname,
        String role
) { }

