package goorm.athena.domain.user.dto.response;

public record UserDetailResponse (
        Long id,
        String nickname,
        String sellerIntroduction,
        String linkUrl
){ }
