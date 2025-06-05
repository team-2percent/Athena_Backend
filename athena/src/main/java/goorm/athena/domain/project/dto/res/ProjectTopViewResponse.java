package goorm.athena.domain.project.dto.res;

public record ProjectTopViewResponse(
        String sellerName,
        String title,
        String description,
        String imageUrl,
        Long achievementRate,
        Long projectId
) {
}

// {판매자, 이름, 소개, 대표 사진, 달성률, 프로젝트 아이디}}