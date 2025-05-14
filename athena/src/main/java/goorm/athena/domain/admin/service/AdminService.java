package goorm.athena.domain.admin.service;

import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import goorm.athena.domain.admin.dto.res.SettlementSummaryPageResponse;
import goorm.athena.domain.admin.repository.AdminQueryRepository;
import goorm.athena.domain.settlement.dto.res.SettlementSummaryResponse;
import goorm.athena.domain.settlement.entity.Status;
import goorm.athena.domain.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminQueryRepository adminProjectQueryService;
    private final SettlementService settlementService;

    public ProjectSummaryResponse getProjectList(String keyword, String sortDirection, int page) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.DESC);
        Sort sort = Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, 10, sort);

        Page<ProjectSummaryResponse.Item> pageData = adminProjectQueryService.findProjectList(keyword, pageable);

        return ProjectSummaryResponse.of(pageData);
    }

    public SettlementSummaryPageResponse getSettlementList(Status status, Integer year, Integer month, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "requestedAt"));
        Page<SettlementSummaryResponse> result = settlementService.getSettlements(status, year, month, pageable);

        return SettlementSummaryPageResponse.of(result);
    }
}
