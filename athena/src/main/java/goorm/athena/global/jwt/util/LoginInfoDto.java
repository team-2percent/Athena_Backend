package goorm.athena.global.jwt.util;

import goorm.athena.domain.user.entity.Role;

public record LoginInfoDto(
        Long userId,
        String nickname,
        Role role
) { }

