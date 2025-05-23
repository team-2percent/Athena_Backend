package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;

public record UserUpdateRequest(
        @Column(length = 50)
        String nickname,

        @Column(length = 200)
        String sellerIntroduction,

        @Column(length = 2000)
        String linkUrl
) { }
