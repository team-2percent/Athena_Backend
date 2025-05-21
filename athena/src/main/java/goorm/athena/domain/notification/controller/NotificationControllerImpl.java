package goorm.athena.domain.notification.controller;

import goorm.athena.domain.notification.dto.NotificationResponse;
import goorm.athena.domain.notification.service.NotificationService;
import goorm.athena.domain.notification.util.EmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
public class NotificationControllerImpl implements NotificationController {
  private final NotificationService notificationService;
  private final EmitterService emitterService;

  @Override
  public SseEmitter subscribe(@RequestParam Long userId){
    return emitterService.connect(userId);
  }

  @Override
  public ResponseEntity<List<NotificationResponse>> getNotifications(@RequestParam Long userId){
    return ResponseEntity.ok(notificationService.getAllNotifications(userId));
  }

  @Override
  public ResponseEntity<Void> readNotification(@PathVariable Long id){
    notificationService.markAsRead(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteNotification(@PathVariable Long id){
    notificationService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteAllNotifications(@RequestParam Long userId){
    notificationService.deleteAllByUser(userId);
    return ResponseEntity.noContent().build();
  }

}