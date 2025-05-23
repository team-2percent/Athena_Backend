package goorm.athena.domain.user.dto.request;

import jakarta.persistence.Column;

public record UserCreateRequest(

        @Column(length = 50)
        String email,

        @Column(length = 100)
        String password,

        @Column(length = 50)
        String nickname
) {
}
