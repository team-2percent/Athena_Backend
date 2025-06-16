package goorm.athena.domain.user.dto.response;

public record UserUpdateResponse(
        Long id,
        String nickname,
        String sellerIntroduction,
        String linkUrl
) { }
