package goorm.athena.domain.notification.service;

import goorm.athena.domain.notification.entity.Notification;
import goorm.athena.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import goorm.athena.domain.notification.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository notificationRepository;

  /**
   * @param urlPath -> nullable
   */
  public Notification createNotification(Long userId, String content, NotificationType type, String urlPath) {
    Notification notification = new Notification(userId, content, type, urlPath);
    return notificationRepository.save(notification);
  }

  @Transactional(readOnly = true)
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