package goorm.athena.domain.user.dto.response;

public record UserGetResponse(
        Long id,
        String email,
        String nickname,
        String imageUrl,
        String sellerDescription,
        String linkUrl
) { }
