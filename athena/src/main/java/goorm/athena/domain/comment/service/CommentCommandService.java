package goorm.athena.domain.comment.service;

import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.comment.mapper.CommentMapper;
import goorm.athena.domain.comment.repository.CommentRepository;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentCommandService {
    private final CommentRepository commentRepository;
    private final UserQueryService userQueryService;
    private final ProjectService projectService;

    @Transactional
    public CommentCreateResponse createComment(Long projectId, Long userId, String content) {
        User user = userQueryService.getUser(userId);
        Project project = projectService.getById(projectId);

        boolean alreadyCommented = commentRepository.existsByUserAndProject(user, project);
        if(alreadyCommented){
            throw new CustomException(ErrorCode.ALREADY_COMMENTED);
        }

        Comment comment = Comment.create(user, project, content);

        commentRepository.save(comment);

        return CommentMapper.toCreateResponse(comment);
    }
}
