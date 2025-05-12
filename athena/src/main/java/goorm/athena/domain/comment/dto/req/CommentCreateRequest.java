package goorm.athena.domain.comment.dto.req;

public record CommentCreateRequest(
        Long projectId,
        String content
) { }
