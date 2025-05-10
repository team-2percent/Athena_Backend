package goorm.athena.domain.comment.controller;

import goorm.athena.domain.comment.dto.req.CommentCreateRequest;
import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.service.CommentService;
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

    @Override
    @PostMapping("/create")
    public ResponseEntity<CommentCreateResponse> createComment(@CheckLogin LoginUserRequest loginUserRequest,
                                                               CommentCreateRequest commentCreateRequest){
        CommentCreateResponse response = commentService.createComment(commentCreateRequest.projectId(),
                loginUserRequest.userId(),
                commentCreateRequest.content());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{projectId}")
    public ResponseEntity<List<CommentGetResponse>> getComment(@PathVariable Long projectId){
        List<CommentGetResponse> response = commentService.getCommentByProject(projectId);
        return ResponseEntity.ok(response);
    }
}
