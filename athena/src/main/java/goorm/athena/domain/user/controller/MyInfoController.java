package goorm.athena.domain.user.controller;

import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.user.dto.request.UserIdValidateRequest;
import goorm.athena.domain.user.dto.request.UserPasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my")
public interface MyInfoController {

    @Operation(summary = "유저 작성 댓글 조회 API", description = "유저가 작성한 댓글들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "유저 작성 댓글 조회 성공")
    @GetMapping("/comments")
    public List<CommentGetResponse> getComments(@Parameter(hidden = true) @CheckLogin LoginUserRequest request);

    @Operation(summary = "유저 비밀번호 확인 API", description = "유저의 비밀번호를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "유저 비밀번호 확인 성공")
    @PostMapping("/checkPassword")
    public ResponseEntity<Boolean> checkPassword(@Parameter(hidden = true) @CheckLogin LoginUserRequest request,
                                                 @RequestBody @Valid UserPasswordRequest passwordRequest);

    @Operation(summary = "유저 비밀번호 갱신 API", description = "새 비밀번호로 유저의 비밀번호를 갱신합니다.")
    @ApiResponse(responseCode = "204", description = "유저 비밀번호 갱신 성공")
    public ResponseEntity<Void> updatePassword(@Parameter(hidden = true) @CheckLogin LoginUserRequest request,
                                               @RequestBody @Valid UserUpdatePasswordRequest updatePassword);

    @Operation(summary = "유저Id 확인 API", description = "유저의 ID를 검증합니다.")
    @ApiResponse(responseCode = "200", description = "유저 ID 검증 성공")
    @GetMapping("/checkUserId")
    public ResponseEntity<Boolean> checkUserId(@Parameter(hidden = true) @CheckLogin LoginUserRequest request,
                                               @RequestParam("userId") Long userId);

    @Operation(summary = "유저 쿠폰 커서 페이지 조회 APi", description = "유저가 현재 보유중인 모든 쿠폰들을 무한 페이징 형식으로 조회합니다.<br>" +
            "조회가 완료되면 아래의 'nextCouponId'를 위에 입력하면 해당 값을 기준으로 다음 값들이 사이즈만큼 보여집니다.")
    @ApiResponse(responseCode = "200", description = "유저가 보유하는 모든 쿠폰들을 페이지 형식으로 조회합니다.")
    @GetMapping("/coupons")
    public ResponseEntity<UserCouponCursorResponse> getUserCoupons(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
            @RequestParam(required = false) Long cursorId,
            @Parameter(hidden = true) @RequestParam(defaultValue = "5") int size
    );
}
