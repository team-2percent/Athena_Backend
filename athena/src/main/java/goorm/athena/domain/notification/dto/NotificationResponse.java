package goorm.athena.domain.notification.dto;

import goorm.athena.domain.notification.service.NotificationMessage;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String content,
        NotificationMessage.NotificationType notificationType,
        Boolean isRead,
        LocalDateTime createdAt,
        String urlPath

) { }
