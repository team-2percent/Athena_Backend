package goorm.athena.domain.user.mapper;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;

public interface RefreshTokenMapper {
    RefreshTokenResponse toRefreshTokenResponse(Long userId, String accessToken, String refreshToken);
}
