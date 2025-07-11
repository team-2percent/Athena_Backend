package goorm.athena.domain.comment.controller;

import goorm.athena.domain.comment.dto.req.CommentCreateRequest;
import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.service.CommentCommandService;
import goorm.athena.domain.comment.service.CommentQueryService;
import goorm.athena.domain.notification.service.NotificationService;
import goorm.athena.domain.project.service.ProjectQueryService;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/comment")
public class CommentControllerImpl implements CommentController{
    private final ProjectQueryService projectQueryService;
    private final UserQueryService userQueryService;
    private final CommentQueryService commentQueryService;
    private final CommentCommandService commentCommandService;
    private final NotificationService notificationService;

    @Override
    @PostMapping("/create")
    public ResponseEntity<CommentCreateResponse> createComment(@CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody CommentCreateRequest commentCreateRequest){
        CommentCreateResponse response = commentCommandService.createComment(commentCreateRequest.projectId(),
                loginUserRequest.userId(),
                commentCreateRequest.content());

        Long projectId = commentCreateRequest.projectId();
        notificationService.notifyReview(userQueryService.getSellerByProjectId(projectId), projectQueryService.getById(projectId).getTitle());

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{projectId}")
    public ResponseEntity<List<CommentGetResponse>> getComment(@PathVariable Long projectId){
        List<CommentGetResponse> response = commentQueryService.getCommentByProject(projectId);
        return ResponseEntity.ok(response);
    }
}
