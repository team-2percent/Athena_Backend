package goorm.athena.domain.notification.dto;

public record FcmLoginRequest (
        Long userId,
        String token
){ }
