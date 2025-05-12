package goorm.athena.domain.project.dto.res;

import lombok.Builder;

@Builder
public record ProjectIdResponse (
    Long projectId,
    Long imageGroupId
){ }
