package goorm.athena.domain.project.dto.cursor;

import java.time.LocalDateTime;

public interface CreatedAtCursorIdentifiable {
    Long id();
    LocalDateTime createdAt();
}
