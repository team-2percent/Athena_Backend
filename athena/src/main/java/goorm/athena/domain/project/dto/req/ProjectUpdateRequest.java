package goorm.athena.domain.project.dto.req;

import goorm.athena.domain.image.dto.req.ImageUpdateRequest;
import goorm.athena.domain.product.dto.req.ProductRequest;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectUpdateRequest (
        Long categoryId,

        String title,
        String description,
        Long goalAmount,
        String contentMarkdown,

        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime shippedAt,

        List<ProductRequest> products   // 상품 리스트
){
}
