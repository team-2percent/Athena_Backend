package goorm.athena.domain.search.mapper;

import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.search.dto.Response.SearchResultResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class SearchMapper {
  public static SearchResultResponse toSearchResultResponse(Page<Project> page) {
    List<SearchResultResponse.Project> projects = page.getContent().stream()
        .map(SearchMapper::toProjectDto)
        .toList();

    return new SearchResultResponse(
        projects,
        page.getTotalElements(),
        page.getSize(),
        page.getNumber(),
        page.getTotalPages());
  }

  public static SearchResultResponse.Project toProjectDto(Project project) {
    // sellerName, thumbnailUrl, daysLeft 등은 조인/계산 필요
    return new SearchResultResponse.Project(
        project.getId(),
        project.getSeller().getNickname(),
        // ToDo 추후 이미지 기능 구현됐을 때, 썸네일 이미지 조회 추가
        // project.getThumbnailUrl(),
        project.getTitle(),
        project.getDescription(),
        project.getGoalAmount(),
        project.getTotalAmount(),
        calculateDaysLeft(project.getStartAt(), project.getEndAt()),
        project.getStatus().name());
  }

  private static int calculateDaysLeft(LocalDateTime startAt, LocalDateTime endAt) {
    return (int) ChronoUnit.DAYS.between(LocalDate.now(), endAt.toLocalDate());
  }
}
