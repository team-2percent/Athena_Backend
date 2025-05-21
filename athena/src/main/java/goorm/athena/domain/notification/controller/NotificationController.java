package goorm.athena.domain.notification.controller;

import goorm.athena.domain.notification.dto.NotificationResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import java.util.List;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Notification", description = "알림 관련 API")
@RequestMapping("/api/notification")
public interface NotificationController {

  @Operation(summary = "SSE 연결 설정", description = "Client가 Server에 SSE 연결을 요청을 합니다.")
  @ApiResponse(responseCode = "200", description = "SSE 연결 성공")
  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@RequestParam Long userId);

  @Operation(summary = "알림 전체 조회 API (최신순)", description = "알림을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "알림 조회 성공")
  @GetMapping("/")
  public ResponseEntity<List<NotificationResponse>> getNotifications(@RequestParam Long userId);

  @Operation(summary = "단일 알림 읽음 처리 API", description = "특정 알림을 읽음 처리합니다.")
  @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공")
  @PostMapping("/{id}/read")
  public ResponseEntity<Void> readNotification(@PathVariable Long id);

  @Operation(summary = "단일 알림 삭제 API", description = "특정 알림을 삭제합니다.")
  @ApiResponse(responseCode = "200", description = "단일 알림 삭제 성공")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteNotification(@PathVariable Long id);

  @Operation(summary = "알림 전체 삭제 API", description = "알림을 전체 삭제합니다.")
  @ApiResponse(responseCode = "200", description = "알림 전체 삭제 성공")
  @DeleteMapping("/")
  public ResponseEntity<Void> deleteAllNotifications(@RequestParam Long userId);
}
