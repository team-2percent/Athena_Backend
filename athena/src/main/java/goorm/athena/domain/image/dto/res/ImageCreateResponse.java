package goorm.athena.domain.image.dto.res;

import lombok.Builder;

@Builder
public record ImageCreateResponse (
        Long id,
        Long imageGroupId,
        String fileName,
        String originalUrl,
        String fileType
){ }