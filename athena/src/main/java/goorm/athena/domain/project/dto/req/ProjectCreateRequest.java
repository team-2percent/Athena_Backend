package goorm.athena.domain.project.dto.req;

import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectCreateRequest(
        Long sellerId,
        Long categoryId,
        Long imageGroupId,
        Long bankAccountId,

        @NotBlank
        @Column(length = 50)
        String title,

        @Size(min = 10, max = 100)
        String description,

        @Column(length = 100000000)
        Long goalAmount,

        @Size(max = 20000)
        String contentMarkdown,

        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime shippedAt,

        String platformPlan,                // 요금제 구독 상태
        List<ProductRequest> products       // 상품 리스트

) { }
