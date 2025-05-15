package goorm.athena.domain.notification.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import goorm.athena.domain.notification.util.NotificationMessage.NotificationType;

@Entity
@Table(name = "notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String content; // 알림 내용

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type; // 알림 종류(쿠폰, 일반 등)

  @Column(name = "is_read", nullable = false)
  private Boolean isRead;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = true)
  private String urlPath;

  public Notification(Long userId, String content, NotificationType type, String urlPath) {
    this.userId = userId;
    this.content = content;
    this.type = type;
    this.isRead = false;
    this.createdAt = LocalDateTime.now();
    this.urlPath = urlPath;
  }

  public void markAsRead() {
    this.isRead = true;
  }
}