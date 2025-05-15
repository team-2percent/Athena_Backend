package goorm.athena.domain.user.dto.response;

import goorm.athena.domain.user.entity.Role;

public record UserGetResponse(
        Long id,
        String email,
        String nickname,
        String imageUrl,
        String sellerDescription
) { }
