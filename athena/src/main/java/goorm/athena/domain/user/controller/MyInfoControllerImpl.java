package goorm.athena.domain.user.controller;

import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class MyInfoControllerImpl implements MyInfoController{
    private final CommentService commentService;

    @Override
    @GetMapping("/comments")
    public List<CommentGetResponse> getComments(@CheckLogin LoginUserRequest request){
        return commentService.getCommentByUser(request.userId());
    }
}
