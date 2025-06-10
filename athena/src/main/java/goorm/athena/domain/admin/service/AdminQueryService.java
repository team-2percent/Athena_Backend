package goorm.athena.domain.admin.service;

import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import goorm.athena.domain.admin.dto.res.SettlementSummaryPageResponse;
import goorm.athena.domain.admin.repository.AdminQueryRepository;
import goorm.athena.domain.settlement.dto.res.SettlementSummaryResponse;
import goorm.athena.domain.settlement.entity.Status;
import goorm.athena.domain.settlement.service.SettlementQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminQueryService {
    private final AdminQueryRepository adminProjectQueryService;
    private final SettlementQueryService settlementQueryService;

    public ProjectSummaryResponse getProjectList(String keyword, String sortBy, String sortDirection, int page) {
        Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection).orElse(Sort.Direction.DESC);
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "createdAt");
        Pageable pageable = PageRequest.of(page, 10, sort);

        return adminProjectQueryService.findProjectList(keyword, pageable);
    }

    public SettlementSummaryPageResponse getSettlementList(Status status, Integer year, Integer month, int page) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "requestedAt"));
        Page<SettlementSummaryResponse> result = settlementQueryService.getSettlements(status, year, month, pageable);

        return SettlementSummaryPageResponse.of(result);
    }
}
