package goorm.athena.domain.comment.dto.req;

import jakarta.persistence.Column;

public record CommentCreateRequest(
        Long projectId,

        @Column(length = 1000, nullable = false)
        String content
) { }
