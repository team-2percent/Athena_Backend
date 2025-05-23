package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;

public record UserLoginRequest(
        @Column(length = 50)
        String email,

        @Column(length = 100)
        String password
) { }
