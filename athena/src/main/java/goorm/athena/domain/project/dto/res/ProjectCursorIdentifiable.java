package goorm.athena.domain.project.dto.res;

import java.time.LocalDateTime;

public interface ProjectCursorIdentifiable {
    Long id();
    LocalDateTime startAt();
}
