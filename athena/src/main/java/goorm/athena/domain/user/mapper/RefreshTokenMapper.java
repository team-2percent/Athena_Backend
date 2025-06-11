package goorm.athena.domain.user.mapper;

import goorm.athena.domain.user.dto.response.RefreshTokenResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RefreshTokenMapper {
    RefreshTokenResponse toRefreshTokenResponse(Long userId, String accessToken, String refreshToken);
}
