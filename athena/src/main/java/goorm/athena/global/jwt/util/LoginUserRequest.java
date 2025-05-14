package goorm.athena.global.jwt.util;

import goorm.athena.domain.user.entity.Role;

public record LoginUserRequest(
        String nickname,
        Long userId,
        Role role
) { }
