package goorm.athena.domain.admin.service;

import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminQueryService adminProjectQueryService;

    public ProjectSummaryResponse getProjectList(String keyword, String sortDirection, int page) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.DESC);
        Sort sort = Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, 10, sort);

        Page<ProjectSummaryResponse.Item> pageData = adminProjectQueryService.findProjectList(keyword, pageable);

        return ProjectSummaryResponse.of(pageData);
    }
}
