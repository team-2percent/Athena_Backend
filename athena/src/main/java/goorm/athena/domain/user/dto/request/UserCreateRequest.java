package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(

        @NotBlank
        @Column(length = 50)
        String email,

        @NotBlank
        @Size(min = 3, max = 100)
        @Column(length = 100)
        String password,

        @NotBlank
        @Size(min = 1, max = 50)
        @Column(length = 50)
        String nickname
) {
}
