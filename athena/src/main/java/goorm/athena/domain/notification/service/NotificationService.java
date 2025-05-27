package goorm.athena.domain.notification.service;

import goorm.athena.domain.notification.dto.NotificationResponse;
import goorm.athena.domain.notification.entity.Notification;
import goorm.athena.domain.notification.mapper.NotificationMapper;
import goorm.athena.domain.notification.repository.NotificationRepository;
import goorm.athena.domain.notification.util.EmitterService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import goorm.athena.domain.notification.service.NotificationMessage.NotificationType;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final EmitterService emitterService;

  /*
   *  내부 공통 알림 생성 + SSE 전송
   */
  private void notify(Long userId, NotificationType type, String urlPath, Object... args) {
    // 1. 알림 메시지 생성 (NotificationMessege 템플릿 기반)
    String content = NotificationMessage.getMessage(type, args);

    // 2. 알림 엔티티 생성 및 DB 저장
    Notification notification = new Notification(userId, content, type, urlPath);
    notificationRepository.save(notification);

    // 3. DTO로 변환 후 실시간 알림 전송
    NotificationResponse response = NotificationMapper.toDto(notification);
    emitterService.sendToUser(userId, response);
  }

  @Transactional
  // 결제 이벤트 알림 (구매자 + 판매자)
  public void notifyPayment(Long buyerId, Long sellerId) {
    // 구매자: 결제 완료
    notify(buyerId, NotificationType.PROJECT_SOLD, "/my");
    // 판매자: 주문 알림
    notify(sellerId, NotificationType.ORDERED, "/my");
  }

  @Transactional
  // 후기 등록 알림 (판매자)
  public void notifyReview(Long sellerId, Long projectId) {
    notify(sellerId, NotificationType.REVIEW, "/project/" + projectId);
  }

  @Transactional
  // 쿠폰 발행 알림 (전체)
  public void notifyCouponToAll(List<Long> userIds, String couponName) {
    for (Long userId : userIds) {
      notify(userId, NotificationType.COUPON, "");
    }
  }

  // 사용자 알림 목록 조회
  public List<NotificationResponse> getAllNotifications(Long userId) {
    return notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(NotificationMapper::toDto)
            .toList();
  }

  // 읽음 처리
  public void markAsRead(Long id) {
    Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
    notification.markAsRead();
  }

  // 단일 알림 삭제
  public void delete(Long id) {
    notificationRepository.deleteById(id);
  }

  // 전체 알림 삭제
  public void deleteAllByUser(Long userId) {
    notificationRepository.deleteAllByUserId(userId);
  }
}