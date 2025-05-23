package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;

public record UserLoginRequest(

        @Column(length = 50)
        String email,

        @Size(min = 4, max = 100)
        @Column(length = 100)
        String password
) { }
