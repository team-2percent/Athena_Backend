package goorm.athena.domain.user.controller;

import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.user.dto.request.UserPasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.user.dto.response.UserSummaryResponse;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.user.dto.response.MyOrderScrollResponse;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Tag(name = "MyPage", description = "마이페이지 관련 API")
public interface UserInfoController {

    @Operation(
            summary = "내 프로젝트 목록 무한스크롤 조회",
            description = """
                    내가 만든 프로젝트를 최신순으로 커서 기반 페이징으로 조회합니다.<br>  
                    • `nextCursorValue`와 `nextProjectId`는 다음 페이지 요청 시 커서로 사용됩니다. <br> 
                    • 프로젝트 상태는 `ACTIVE`가 먼저, 이후 `COMPLETED` 상태로 정렬됩니다.
                    • 첫 페이지 입장시 해당 요청은 빈값으로 요청해서 초기 데이터들을 가져옵니다 
                    """
    )
    @GetMapping("/api/my/project")
    ResponseEntity<MyProjectScrollResponse> getMyProjects(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
            @Parameter(description = "커서: 마지막 항목의 createdAt", example = "2025-05-10T10:00:00")
            @RequestParam(required = false) LocalDateTime nextCursorValue,
            @Parameter(description = "커서: 마지막 항목의 projectId", example = "123")
            @RequestParam(required = false) Long nextProjectId,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    );

    @Operation(
            summary = "내 구매 상품 목록 무한스크롤 조회",
            description = """
                    내가 주문한 상품을 최신순으로 커서 기반 페이징으로 조회합니다.<br>  
                    • `nextCursorValue`와 `nextOrderId`는 다음 페이지 요청 시 커서로 사용됩니다. <br> 
                    • 주문 상태가 `ORDERED` 또는 `COMPLETED`인 항목만 조회됩니다.<br>
                    • 첫 페이지 입장 시 이 값들은 null로 요청합니다.
                    """
    )
    @GetMapping("/api/my/order")
    ResponseEntity<MyOrderScrollResponse> getMyOrders(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
            @Parameter(description = "커서: 마지막 항목의 orderedAt", example = "2025-05-14T15:30:00")
            @RequestParam(required = false) LocalDateTime nextCursorValue,
            @Parameter(description = "커서: 마지막 항목의 orderId", example = "456")
            @RequestParam(required = false) Long nextOrderId,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    );
  
    @Operation(summary = "유저 소개 조회 API", description = "유저가 작성한 소개를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "유저 소개 조회 성공")
    @GetMapping("/info")
    public ResponseEntity<UserSummaryResponse> getSummary(@Parameter(hidden = true) @CheckLogin LoginUserRequest request);

    @Operation(summary = "유저 작성 댓글 조회 API", description = "유저가 작성한 댓글들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "유저 작성 댓글 조회 성공")
    @GetMapping("/comment")
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
    @GetMapping("/coupon")
    public ResponseEntity<UserCouponCursorResponse> getUserCoupons(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
            @RequestParam(required = false) Long cursorId,
            @Parameter(hidden = true) @RequestParam(defaultValue = "5") int size
    );

}
