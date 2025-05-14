package goorm.athena.domain.notification.controller;

import goorm.athena.domain.notification.entity.Notification;
import goorm.athena.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationControllerImpl implements NotificationController {
  private final NotificationService notificationService;

  @Override
  public ResponseEntity<List<Notification>> getNotifications(@RequestParam Long userId) {
    return ResponseEntity.ok(notificationService.getNotifications(userId));
  }

  @Override
  public ResponseEntity<Void> readNotification(@RequestParam Long notificationId) {
    notificationService.readNotification(notificationId);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> deleteNotification(@RequestParam Long notificationId) {
    notificationService.deleteNotification(notificationId);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<Void> deleteAllNotifications(@RequestParam Long userId) {
    notificationService.deleteAllNotifications(userId);
    return ResponseEntity.ok().build();
  }
}