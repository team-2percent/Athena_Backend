package goorm.athena.domain.notification.service;

import java.time.LocalDateTime;
import java.util.List;
import java.time.temporal.ChronoUnit;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.ProjectRepository;

@Service
public class NotificationScheduler {
  private final NotificationService notificationService;
  private final ProjectRepository projectRepository;

  public NotificationScheduler(NotificationService notificationService, ProjectRepository projectRepository) {
    this.notificationService = notificationService;
    this.projectRepository = projectRepository;
  }

  /*
   * 일 기준 프로젝트 마감일이 정확히 7,3,1일 남은 프로젝트 조회하여, 마감임박 알림 전송
   */
  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void sendProjectEndNotificationAll() {
    LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    List<LocalDateTime> endDates = List.of(now.plusDays(7), now.plusDays(3), now.plusDays(1));
    List<Project> projects = projectRepository.findByEndAtIn(endDates);

    for (Project project : projects) {
      long daysLeft = ChronoUnit.DAYS.between(now, project.getEndAt());
      notificationService.sendProjectEndNotification(project.getSeller().getId(), project.getId(),
          project.getTitle(),
          (int) daysLeft);
    }
  }
}