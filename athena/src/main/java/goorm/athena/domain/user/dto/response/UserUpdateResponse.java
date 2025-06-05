package goorm.athena.domain.user.dto.response;

import goorm.athena.domain.user.entity.Role;

public record UserUpdateResponse(
        Long id,
        String nickname,
        String sellerIntroduction,
        String linkUrl
) { }
