package goorm.athena.domain.user.dto.response;

import goorm.athena.domain.user.entity.Role;

public record UserUpdateResponse(
        Long id,
        String email,
        String password,
        String nickname,
        Role role
) { }
