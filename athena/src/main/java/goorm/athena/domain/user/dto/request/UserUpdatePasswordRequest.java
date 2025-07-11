package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdatePasswordRequest(
        @NotBlank
        @Size(min = 3, max = 100)
        @Column(length = 100)
        String oldPassword,

        @NotBlank
        @Size(min = 3, max = 100)
        @Column(length = 100)
        String newPassword
) {  }