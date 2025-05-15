package goorm.athena.domain.notification.controller;

import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import goorm.athena.domain.notification.entity.Notification;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@Tag(name = "Notification", description = "알림 관련 API")
@RequestMapping("/api/notification")
public interface NotificationController {
  @Operation(summary = "알림 조회 API", description = "알림을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "알림 조회 성공")
  @GetMapping("/")
  public ResponseEntity<List<Notification>> getNotifications(@RequestParam Long userId);

  @Operation(summary = "알림 읽음 처리 API", description = "알림을 읽음 처리합니다.")
  @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
  @PostMapping("/read")
  public ResponseEntity<Void> readNotification(@RequestParam Long notificationId);

  @Operation(summary = "알림 삭제 API", description = "알림을 삭제합니다.")
  @ApiResponse(responseCode = "200", description = "알림 삭제 성공")
  @DeleteMapping("/")
  public ResponseEntity<Void> deleteNotification(@RequestParam Long notificationId);

  @Operation(summary = "알림 전체 삭제 API", description = "알림을 전체 삭제합니다.")
  @ApiResponse(responseCode = "200", description = "알림 전체 삭제 성공")
  @DeleteMapping("/all")
  public ResponseEntity<Void> deleteAllNotifications(@RequestParam Long userId);
}
