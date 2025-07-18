package goorm.athena.domain.comment.service;

import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.comment.mapper.CommentMapper;
import goorm.athena.domain.comment.repository.CommentQueryRepository;
import goorm.athena.domain.comment.repository.CommentRepository;
import goorm.athena.domain.image.service.ImageQueryService;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.service.ProjectQueryService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentQueryService {
    private final CommentQueryRepository commentQueryRepository;
    private final CommentRepository commentRepository;
    private final UserQueryService userQueryService;
    private final ProjectQueryService projectQueryService;
    private final ImageQueryService imageQueryService;
    private final CommentMapper commentMapper;

    @Transactional(readOnly = true)
    public List<CommentGetResponse> getCommentByProject(Long projectId){
        Project project = projectQueryService.getById(projectId);
        // 쿼리 결과를 Object[] 형태로 받아옴 (Comment, 이미지 URL)
        List<Comment> results = commentQueryRepository.getCommentsByProject(project);

        return results.stream()
                .map(comment -> {
                    String imageUrl = imageQueryService.getImage(comment.getUser().getImageGroup().getId());
                    return commentMapper.toGetResponse(comment, imageUrl);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentGetResponse> getCommentByUser(Long userId){
        User user = userQueryService.getUser(userId);
        List<Comment> results = commentQueryRepository.getCommentsByUser(user);

        return results.stream()
                .map(comment -> {
                    String imageUrl = imageQueryService.getImage(comment.getProject().getImageGroup().getId());
                    return commentMapper.toGetResponse(comment, imageUrl);
                })
                .toList();
    }
}
