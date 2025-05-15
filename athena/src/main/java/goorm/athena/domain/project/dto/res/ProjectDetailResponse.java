package goorm.athena.domain.project.dto.res;

import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.user.dto.response.UserDetailResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectDetailResponse (
        Long id,
        String title,
        String description,

        Long goalAmount,
        Long totalAmount,
        String markdown,

        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime shippedAt,

        List<String> imageUrls,     // 대표 이미지 리스트
        UserDetailResponse sellerResponse,
        List<ProductResponse> productResponses
){ }
