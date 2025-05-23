package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;

public record UserUpdatePasswordRequest(
        @NotBlank @Column(length = 100) String oldPassword,
        @NotBlank @Column(length = 100) String newPassword
) {  }