package goorm.athena.domain.settlement.service;

import goorm.athena.domain.admin.dto.res.ProductSettlementSummaryResponse;
import goorm.athena.domain.admin.dto.res.SettlementDetailInfoResponse;
import goorm.athena.domain.admin.dto.res.SettlementHistoryPageResponse;
import goorm.athena.domain.settlement.entity.Status;
import goorm.athena.domain.settlement.dto.res.SettlementSummaryResponse;
import goorm.athena.domain.settlement.repository.SettlementQueryRepository;
import goorm.athena.domain.settlementhistory.repository.SettlementHistoryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettlementQueryService {

    private final SettlementQueryRepository settlementQueryRepository;
    private final SettlementHistoryQueryRepository settlementHistoryQueryRepository;

    public Page<SettlementSummaryResponse> getSettlements(Status status, Integer year, Integer month, Pageable pageable) {
        return settlementQueryRepository.findPageByFilters(status, year, month, pageable);
    }

    public SettlementDetailInfoResponse getSettlementDetailInfo(Long settlementId) {
        return settlementQueryRepository.findSettlementDetailInfo(settlementId);
    }

    public SettlementHistoryPageResponse getSettlementHistories(Long settlementId, int page) {
        Pageable pageable = PageRequest.of(page, 10);
        Page<SettlementHistoryPageResponse.SettlementHistoryItem> pageResult =
                settlementHistoryQueryRepository.findHistoriesBySettlementId(settlementId, pageable);

        return new SettlementHistoryPageResponse(
                pageResult.getContent(),
                new SettlementHistoryPageResponse.PageInfo(pageResult.getNumber(), pageResult.getTotalPages())
        );
    }

    public ProductSettlementSummaryResponse getProductSettlementInfo(Long settlementId) {
        return settlementQueryRepository.findProductSettlementsWithSummary(settlementId);
    }
}
