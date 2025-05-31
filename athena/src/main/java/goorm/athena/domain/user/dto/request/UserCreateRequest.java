package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

public record UserCreateRequest(

        @NotBlank
        @Email(message= "유효한 이메일 형식이어야 합니다.")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}\\p{M}]*$", message = "이모지를 포함할 수 없습니다.")
        @Size(max=50)
        String email,

        @NotBlank
        @Size(min = 3, max = 100)
        @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}\\p{M}]*$", message = "이모지를 포함할 수 없습니다.")
        @Column(length = 100)
        String password,

        @NotBlank
        @Size(min = 1, max = 50)
        @Pattern(regexp = "^[\\p{L}\\p{N}\\p{P}\\p{Z}\\p{M}]*$", message = "이모지를 포함할 수 없습니다.")
        @Column(length = 50)
        String nickname
) {
}
