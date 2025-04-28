package goorm.athena.domain.novel.dto.req;

import goorm.athena.domain.novel.entity.Status;

public record NovelCreateRequest(
        Long userId,
        String title,
        String summary,
        Status status,
        Long imageId
) {

}