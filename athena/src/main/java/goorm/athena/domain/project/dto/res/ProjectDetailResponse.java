package goorm.athena.domain.project.dto.res;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.project.entity.Status;
import goorm.athena.domain.user.dto.response.UserDetailResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectDetailResponse (
        Long id,
        Long category,
        String title,
        String description,

        Long goalAmount,
        Long totalAmount,
        String convertedMarkdown,

        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime shippedAt,
        LocalDateTime createdAt,

        List<String> imageUrls,     // 대표 이미지 리스트
        UserDetailResponse sellerResponse,
        List<ProductResponse> productResponses,

        Status status
){ }
