package goorm.athena.domain.search.mapper;

import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.search.dto.response.SearchResultResponse;
import goorm.athena.domain.search.service.SearchService;
import java.util.List;
import org.springframework.data.domain.Page;

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
    return new SearchResultResponse.Project(
        project.getId(),
        project.getSeller().getNickname(),
        // ToDo 추후 이미지 기능 구현됐을 때, 썸네일 이미지 조회 추가
        // project.getThumbnailUrl(),
        project.getTitle(),
        project.getDescription(),
        project.getGoalAmount(),
        project.getTotalAmount(),
        SearchService.calculateDaysLeft(project.getStartAt(), project.getEndAt()),
        project.getStatus().name());
  }
}
