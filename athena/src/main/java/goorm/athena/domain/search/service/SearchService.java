package goorm.athena.domain.search.service;

import goorm.athena.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.spec.ProjectSpecification;
import goorm.athena.domain.search.dto.response.SearchResultResponse;
import goorm.athena.domain.search.mapper.SearchMapper;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RequiredArgsConstructor
@Service
public class SearchService {
  private final ProjectRepository projectRepository;

  public SearchResultResponse getList(Integer page, String searchWord) {
    Pageable pageable = PageRequest.of(page, 20, Sort.by(Sort.Order.desc("createdDate")));
    Specification<Project> spec = ProjectSpecification.searchByTitleOrSeller(searchWord);
    Page<Project> projectPage = projectRepository.findAll(spec, pageable);
    return SearchMapper.toSearchResultResponse(projectPage);
  }

  public static int calculateDaysLeft(LocalDateTime startAt, LocalDateTime endAt) {
    if (startAt == null || endAt == null) {
      throw new IllegalArgumentException("startAt 또는 endAt은 null이 될 수 없습니다.");
    }
    return (int) ChronoUnit.DAYS.between(LocalDate.now(), endAt.toLocalDate());
  }
}
