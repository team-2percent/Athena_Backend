package goorm.athena.domain.settlementhistory.repository;

import goorm.athena.domain.settlementhistory.entity.SettlementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementHistoryRepository extends JpaRepository<SettlementHistory, Long> {

}