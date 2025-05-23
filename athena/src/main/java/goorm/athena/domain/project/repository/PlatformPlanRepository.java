package goorm.athena.domain.project.repository;

import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformPlanRepository extends JpaRepository<PlatformPlan, Long> {
    PlatformPlan findByName(PlanName name);
    boolean existsByName(PlanName name);
}