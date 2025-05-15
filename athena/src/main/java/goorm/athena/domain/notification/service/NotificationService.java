package goorm.athena.domain.notification.service;

import goorm.athena.domain.notification.entity.Notification;
import goorm.athena.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import goorm.athena.domain.notification.util.NotificationMessage.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import goorm.athena.domain.notification.util.NotificationMessage;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;

  /**
   * @param urlPath -> nullable
   */
  public Notification createNotification(Long userId, String content, NotificationType notificationType,
      String urlPath) {
    Notification notification = new Notification(userId, content, notificationType, urlPath);
    return notificationRepository.save(notification);
  }

  public void sendCouponEventNotification(Long userId, String couponEventTitle) {
    String content = NotificationMessage.getMessage(NotificationType.COUPON, couponEventTitle);
    createNotification(userId, content, NotificationType.COUPON, null);
  }

  // ToDo: 팔로우 기능 구현 이후 주석 해제
  // public void sendFollowNotification(Long userId, String followerName, Long
  // followerId) {
  // String content = NotificationMessage.getMessage(NotificationType.FOLLOW,
  // followerName);
  // String urlPath = "/user/" + followerId;
  // createNotification(userId, content, NotificationType.FOLLOW, urlPath);
  // }

  public void sendProjectEndNotification(Long userId, Long projectId, String projectTitle, int daysLeft) {
    String content = NotificationMessage.getMessage(NotificationType.PROJECT_END, projectTitle, daysLeft);
    String urlPath = "/project/" + projectId;
    createNotification(userId, content, NotificationType.PROJECT_END, urlPath);
  }

  // ToDo: urlPath를 별도로 받지 않고, 메시지 자체에서 하이퍼링크를 걸어서 프론트에 반환할 수 있도록 수정
  public void sendOrderedNotification(Long sellerId, String buyerName, String projectTitle) {
    String content = NotificationMessage.getMessage(NotificationType.ORDERED, buyerName, projectTitle);
    createNotification(sellerId, content, NotificationType.ORDERED, null);
  }

  @Transactional
  public void readNotification(Long notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
    notification.markAsRead();
  }

  public List<Notification> getNotifications(Long userId) {
    return notificationRepository.findByUserId(userId);
  }

  public void deleteNotification(Long notificationId) {
    notificationRepository.deleteById(notificationId);
  }

  public void deleteAllNotifications(Long userId) {
    notificationRepository.deleteAllByUserId(userId);
  }
}