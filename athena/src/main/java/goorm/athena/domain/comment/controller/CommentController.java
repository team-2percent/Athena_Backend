package goorm.athena.domain.comment.controller;

import goorm.athena.domain.comment.dto.req.CommentCreateRequest;
import goorm.athena.domain.comment.dto.res.CommentCreateResponse;
import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Tag(name = "comment", description = "댓글(후기) 관련 API")
@RequestMapping("/api/comment")
public interface CommentController {

    @Operation(summary = "댓글 작성 API", description = "입력된 정보로 댓글을 작성합니다.<br>" +
            "해당 프로젝트를 구매한 유저만 댓글을 작성할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "해당 프로젝트의 새 댓글이 작성되었습니다.",
            content = @Content(schema = @Schema(implementation = CommentCreateResponse.class)))
    @PostMapping("/create")
    public ResponseEntity<CommentCreateResponse> createComment(@Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
                                                               CommentCreateRequest commentCreateRequest);
    @Operation(summary = "댓글 조회 API", description = "해당 프로젝트 ID의 댓글들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "프로젝트의 댓글들을 조회했습니다.",
            content = @Content(schema = @Schema(implementation = CommentGetResponse.class)))
    @GetMapping("/{projectId}")
    public ResponseEntity<List<CommentGetResponse>> getComment(@PathVariable Long projectId);
}
