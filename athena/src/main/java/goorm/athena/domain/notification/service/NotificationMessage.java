package goorm.athena.domain.notification.service;

import java.util.HashMap;
import java.util.Map;

public class NotificationMessage {
  public enum NotificationType {
    COUPON,
    PROJECT_SOLD,
    ORDERED,
    REVIEW
  }

  private static final Map<NotificationType, String> templates = new HashMap<>();

  static {
    templates.put(NotificationType.COUPON, "🎁 수령할 수 있는 신규 쿠폰이 발행되었어요! 쿠폰명: %s");
    templates.put(NotificationType.ORDERED, "%s님이 회원님의 프로젝트 '%s'를 주문했습니다.");
    templates.put(NotificationType.PROJECT_SOLD, "💸 '%s' 프로젝트가 결제되었습니다!");
    templates.put(NotificationType.REVIEW, "⭐ 회원님의 프로젝트 '%s'에 새로운 후기가 등록되었습니다!");
  }

  public static <T> String getMessage(NotificationType notificationType, T... args) {
    String template = templates.get(notificationType);
    if (template == null) {
      throw new IllegalArgumentException("정의되지 않은 알림 타입입니다: " + notificationType);
    }
    return String.format(template, args);
  }
}
