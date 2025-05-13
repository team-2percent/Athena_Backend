package goorm.athena.domain.project.dto.cursor;

import java.time.LocalDateTime;

public interface DeadLineCursorIdentifiable {
    Long id();
    LocalDateTime endAt();
}
