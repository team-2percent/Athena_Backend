package goorm.athena.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserPasswordRequest(
        @NotBlank String password
) {
}
