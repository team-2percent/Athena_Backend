package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

public record UserPasswordRequest(
        @NotBlank @Column(length = 100) String password
) {
}
