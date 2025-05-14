package goorm.athena.domain.comment.dto.res;

import java.time.LocalDateTime;

public record CommentCreateResponse(
        Long id,
        String userName,
        String content,
        LocalDateTime createdAt
) { }
