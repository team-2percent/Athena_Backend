package goorm.athena.domain.comment.controller;

import goorm.athena.domain.comment.dto.req.CommentCreateRequest;
import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.domain.user.service.UserService;
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
    private final CommentService commentService;
    private final ProjectService projectService;
    private final FcmNotificationService fcmNotificationService;

    @Override
    @PostMapping("/create")
    public ResponseEntity<CommentCreateResponse> createComment(@CheckLogin LoginUserRequest loginUserRequest,
                                                               @RequestBody CommentCreateRequest commentCreateRequest){
        CommentCreateResponse response = commentService.createComment(commentCreateRequest.projectId(),
                loginUserRequest.userId(),
                commentCreateRequest.content());

        Long projectId = commentCreateRequest.projectId();
        fcmNotificationService.notifyReview(projectService.getSellerId(projectId), projectService.getById(projectId).getTitle());

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{projectId}")
    public ResponseEntity<List<CommentGetResponse>> getComment(@PathVariable Long projectId){
        List<CommentGetResponse> response = commentService.getCommentByProject(projectId);
        return ResponseEntity.ok(response);
    }
}
