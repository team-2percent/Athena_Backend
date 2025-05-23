package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @NotBlank
        @Size(min = 1)
        @Column(length = 50)
        String nickname,

        @Column(length = 200)
        String sellerIntroduction,

        @Column(length = 2000)
        String linkUrl
) { }
