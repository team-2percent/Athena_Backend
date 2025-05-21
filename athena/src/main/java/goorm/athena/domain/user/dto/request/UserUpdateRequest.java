package goorm.athena.domain.user.dto.request;

public record UserUpdateRequest(
        String nickname,
        String sellerIntroduction,
        String linkUrl
) { }
