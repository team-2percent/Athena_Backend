package goorm.athena.domain.category.util;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.repository.PlatformPlanRepository;

@Component
public class PlatformPlanDataInitializer implements ApplicationRunner {
  private final PlatformPlanRepository platformPlanRepository;

  public PlatformPlanDataInitializer(PlatformPlanRepository platformPlanRepository) {
    this.platformPlanRepository = platformPlanRepository;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    // 미리 정의된 플랜 목록 및 수수료율 예시
    List<PlatformPlan> defaultPlans = List.of(
        PlatformPlan.builder().name(PlanName.BASIC).platformFeeRate(0.10).pgFeeRate(0.03).vatRate(0.10)
            .description("기본 요금제").build(),
        PlatformPlan.builder().name(PlanName.PRO).platformFeeRate(0.08).pgFeeRate(0.025).vatRate(0.10)
            .description("프로 요금제").build(),
        PlatformPlan.builder().name(PlanName.PREMIUM).platformFeeRate(0.05).pgFeeRate(0.02).vatRate(0.10)
            .description("프리미엄 요금제").build());
    defaultPlans.forEach(plan -> {
      if (!platformPlanRepository.existsByName(plan.getName())) {
        platformPlanRepository.save(plan);
      }
    });
  }
}