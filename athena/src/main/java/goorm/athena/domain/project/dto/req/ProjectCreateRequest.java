package goorm.athena.domain.project.dto.req;

import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectCreateRequest(
        Long sellerId,
        Long categoryId,
        Long imageGroupId,
        Long bankAccountId,

        String title,
        String description,
        Long goalAmount,
        String contentMarkdown,

        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime shippedAt,

        String platformPlan,                // 요금제 구독 상태
        List<ProductRequest> products       // 상품 리스트

        // List<MultipartFile> markdownImages  // 마크다운 첨부 이미지 (로컬)
) { }
