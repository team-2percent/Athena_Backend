package goorm.athena.domain.project.dto.req;

import goorm.athena.domain.product.dto.req.ProductRequest;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectUpdateRequest (
        Long categoryId,
        Long bankAccountId,

        @NotBlank
        @Column(length = 50)
        String title,

        @Size(min = 10, max = 100)
        String description,

        @Column(length = 100000000)
        Long goalAmount,
        String contentMarkdown,

        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime shippedAt,

        List<ProductRequest> products   // 상품 리스트
){
}
