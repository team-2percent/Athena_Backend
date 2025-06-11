package goorm.athena.domain.comment.mapper;

import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import org.checkerframework.checker.units.qual.C;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "project", source = "project")
    Comment toEntity(User user, Project project, String content);

    @Mapping(target = "userName", source = "comment.user.nickname")
    CommentCreateResponse toCreateResponse(Comment comment);
    @Mapping(target = "id", source = "comment.id")
    @Mapping(target = "userName", source = "comment.user.nickname")
    @Mapping(target = "projectName", source = "comment.project.title")
    @Mapping(target = "content", source = "comment.content")
    @Mapping(target = "createdAt", source = "comment.createdAt")
    @Mapping(target = "projectId", source = "comment.project.id")
    CommentGetResponse toGetResponse(Comment comment, String imageUrl);
}
