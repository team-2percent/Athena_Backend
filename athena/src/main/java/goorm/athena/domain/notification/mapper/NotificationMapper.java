package goorm.athena.domain.notification.mapper;

import goorm.athena.domain.notification.dto.NotificationResponse;
import goorm.athena.domain.notification.entity.Notification;


public class NotificationMapper {

    // 알림 전송 DTO
    public static NotificationResponse toDto(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getContent(),
                notification.getType(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getUrlPath()
        );
    }
    
}

