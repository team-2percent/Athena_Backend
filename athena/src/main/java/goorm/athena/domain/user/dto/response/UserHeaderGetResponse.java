package goorm.athena.domain.user.dto.response;

public record UserHeaderGetResponse(
        Long id,
        String nickname,
        String imageUrl
) { }
