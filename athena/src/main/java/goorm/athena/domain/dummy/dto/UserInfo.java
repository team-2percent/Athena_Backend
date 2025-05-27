package goorm.athena.domain.dummy.dto;

public record UserInfo(
        String email,
        String password,
        String nickname,
        String role,
        String introduction,
        String link
) {}