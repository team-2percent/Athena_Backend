package goorm.athena.domain.user.mapper;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;

public class RefreshTokenMapper {

    public static RefreshTokenResponse toRefreshTokenResponse(Long userId, String accessToken, String refreshToken){
        return new RefreshTokenResponse(
                userId,
                accessToken,
                refreshToken);
    }
}
