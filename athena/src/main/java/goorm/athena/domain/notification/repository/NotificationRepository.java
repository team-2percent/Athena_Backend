package goorm.athena.domain.notification.repository;

import goorm.athena.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByUserId(Long userId);

  void deleteAllByUserId(Long userId);
}