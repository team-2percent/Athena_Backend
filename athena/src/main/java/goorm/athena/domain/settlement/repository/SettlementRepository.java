package goorm.athena.domain.settlement.repository;

import goorm.athena.domain.settlement.entity.Settlement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}