package goorm.athena.domain.comment.service;

import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.comment.mapper.CommentMapper;
import goorm.athena.domain.comment.repository.CommentRepository;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserQueryService userQueryService;
    private final ProjectService projectService;
    private final ImageService imageService;

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

    @Transactional(readOnly = true)
    public List<CommentGetResponse> getCommentByProject(Long projectId){
        Project project = projectService.getById(projectId);
        // 쿼리 결과를 Object[] 형태로 받아옴 (Comment, 이미지 URL)
        List<Comment> results = commentRepository.findByProjectWithUserProfileImage(project);

        return results.stream()
                .map(comment -> {
                    String imageUrl = imageService.getImage(comment.getUser().getImageGroup().getId());
                    return CommentMapper.toGetResponse(comment, imageUrl);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentGetResponse> getCommentByUser(Long userId){
        User user = userQueryService.getUser(userId);
        List<Comment> results = commentRepository.findByUserWithProjectImage(user);

        return results.stream()
                .map(comment -> {
                    String imageUrl = imageService.getImage(comment.getProject().getImageGroup().getId());
                    return CommentMapper.toGetResponse(comment, imageUrl);
                })
                .toList();
    }
}
