package goorm.athena.domain.user.controller;

import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.user.dto.request.UserPasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class MyInfoControllerImpl implements MyInfoController{
    private final CommentService commentService;
    private final UserService userService;

    @Override
    @GetMapping("/comments")
    public List<CommentGetResponse> getComments(@CheckLogin LoginUserRequest request){
        return commentService.getCommentByUser(request.userId());
    }

    @Override
    @PostMapping("/checkPassword")
    public boolean checkPassword(@CheckLogin LoginUserRequest request,
                                 @RequestBody @Valid UserPasswordRequest passwordRequest){

        return userService.checkPassword(request.userId(), passwordRequest.password());
    }

    @Override
    @PostMapping("/updatePassword")
    public ResponseEntity<Void> updatePassword(@CheckLogin LoginUserRequest request,
                                                 @RequestBody @Valid UserUpdatePasswordRequest updatePassword){
        userService.updatePassword(request.userId(), updatePassword);

        return ResponseEntity.noContent().build();
    }
}
