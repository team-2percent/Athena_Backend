package goorm.athena.domain.user.controller;

import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my")
public interface MyInfoController {

    @Operation(summary = "유저 작성 댓글 조회 API", description = "유저가 작성한 댓글들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "유저 작성 댓글 조회 성공")
    @GetMapping("/comments")
    public List<CommentGetResponse> getComments(@Parameter(hidden = true) @CheckLogin LoginUserRequest request);
}
