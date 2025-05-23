package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserPasswordRequest(
        @NotBlank
        @Size(min = 4, max = 100)
        @Column(length = 100)
        String password
) {
}
