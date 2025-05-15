package goorm.athena.domain.notification.util;

import java.util.HashMap;
import java.util.Map;

public class NotificationMessage {
  public enum NotificationType {
    COUPON,
    FOLLOW,
    PROJECT_END,
    ORDERED
  }

  private static final Map<NotificationType, String> templates = new HashMap<>();

  static {
    templates.put(NotificationType.COUPON, "수령할 수 있는 신규 쿠폰이 발행되었어요~! 쿠폰이름: %s");
    templates.put(NotificationType.FOLLOW, "%s님이 회원님을 팔로우했습니다.");
    templates.put(NotificationType.PROJECT_END, "마감임박!!! %s 프로젝트가 %d일 남았습니다!");
    templates.put(NotificationType.ORDERED, "%s님이 회원님의 프로젝트 %s를 주문하셨습니다!");
  }

  public static <T> String getMessage(NotificationType notificationType, T... args) {
    String template = templates.get(notificationType);
    if (template == null) {
      throw new IllegalArgumentException("정의되지 않은 알림 타입입니다: " + notificationType);
    }
    return String.format(template, args);
  }
}
