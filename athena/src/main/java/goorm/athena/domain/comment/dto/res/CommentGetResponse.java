package goorm.athena.domain.comment.dto.res;

import java.time.LocalDateTime;

public record CommentGetResponse(
        Long id,
        String userName,
        String content,
        LocalDateTime createdAt,
        Long projectId
) {
}
