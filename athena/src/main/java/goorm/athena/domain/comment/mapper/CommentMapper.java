package goorm.athena.domain.comment.mapper;

import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.entity.Comment;

public class CommentMapper {
    public static CommentCreateResponse toCreateResponse(Comment comment){
        return new CommentCreateResponse(
                comment.getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }

    public static CommentGetResponse toGetResponse(Comment comment){
        return new CommentGetResponse(
                comment.getId(),
                comment.getUser().getNickname(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getProject().getId()
        );
    }
}
