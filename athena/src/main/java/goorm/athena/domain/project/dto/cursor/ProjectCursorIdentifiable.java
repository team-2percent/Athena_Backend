package goorm.athena.domain.project.dto.cursor;

import java.time.LocalDateTime;

public interface ProjectCursorIdentifiable {
    Long id();
    LocalDateTime createdAt();
}
