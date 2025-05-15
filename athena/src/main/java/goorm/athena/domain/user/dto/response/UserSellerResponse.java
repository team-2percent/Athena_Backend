package goorm.athena.domain.user.dto.response;

public record UserSellerResponse(
        Long userId,
        String nickname,
        String sellerIntroduction,
        String imageUrl
) {
}
