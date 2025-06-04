package goorm.athena.domain.project.util;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.PlanName;
import java.util.List;

@Component
public class PlatformPlanDataInitializer implements ApplicationRunner {
  private final PlatformPlanRepository platformPlanRepository;

  public PlatformPlanDataInitializer(PlatformPlanRepository platformPlanRepository) {
    this.platformPlanRepository = platformPlanRepository;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    List<PlatformPlan> defaultPlans = List.of(
        PlatformPlan.builder()
            .name(PlanName.BASIC)
            .platformFeeRate(0.05)
            .pgFeeRate(0.03)
            .vatRate(0.1)
            .description("기본 요금제 - 최소 기능 제공")
            .build(),
        PlatformPlan.builder()
            .name(PlanName.PRO)
            .platformFeeRate(0.09)
            .pgFeeRate(0.03)
            .vatRate(0.1)
            .description("프로 요금제 - 마케팅 도구 포함")
            .build(),
        PlatformPlan.builder()
            .name(PlanName.PREMIUM)
            .platformFeeRate(0.15)
            .pgFeeRate(0.03)
            .vatRate(0.1)
            .description("프리미엄 요금제 - 전체 기능 제공 및 우선 지원")
            .build());
    defaultPlans.forEach(platformPlanRepository::saveIfNotExist);
  }
}
