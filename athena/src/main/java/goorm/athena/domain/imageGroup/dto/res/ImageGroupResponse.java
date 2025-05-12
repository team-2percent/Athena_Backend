package goorm.athena.domain.imageGroup.dto.res;

import goorm.athena.domain.imageGroup.entity.Type;
import lombok.Builder;

// 1차 MVP 끝난 후 제거 예정
@Builder
public record ImageGroupResponse (
        Long id,
        Type type
){  }
