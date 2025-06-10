package goorm.athena.domain.user.controller;

import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.user.dto.request.UserPasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.user.dto.response.UserSummaryResponse;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.userCoupon.service.UserCouponQueryService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import goorm.athena.domain.user.dto.response.MyOrderScrollRequest;
import goorm.athena.domain.user.dto.response.MyOrderScrollResponse;
import goorm.athena.domain.user.dto.response.MyProjectScrollRequest;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class UserInfoControllerImpl implements UserInfoController {
    private final CommentService commentService;
    private final MyInfoService myInfoService;
    private final UserService userService;
    private final UserCouponQueryService userCouponQueryService;

    @Override
    @GetMapping("/info")
    public ResponseEntity<UserSummaryResponse> getSummary(@CheckLogin LoginUserRequest request){
        UserSummaryResponse response = userService.getUserSummary(request.userId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/comment")
    public ResponseEntity<List<CommentGetResponse>> getComments(@CheckLogin LoginUserRequest request){
        List<CommentGetResponse> responses = commentService.getCommentByUser(request.userId());
        return ResponseEntity.ok(responses);
    }

    @Override
    @PostMapping("/checkPassword")
    public ResponseEntity<Boolean> checkPassword(@CheckLogin LoginUserRequest request,
                                 @RequestBody @Valid UserPasswordRequest passwordRequest){

        boolean response = userService.checkPassword(request.userId(), passwordRequest.password());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/updatePassword")
    public ResponseEntity<Void> updatePassword(@CheckLogin LoginUserRequest request,
                                                 @RequestBody @Valid UserUpdatePasswordRequest updatePassword){
        userService.updatePassword(request.userId(), updatePassword);

        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping("/checkUserId")
    public ResponseEntity<Boolean> checkUserId(@CheckLogin LoginUserRequest request,
                                               @RequestParam("userId") Long userId){
        return ResponseEntity.ok(request.userId().equals(userId));
    }

    @Override
    @GetMapping("/coupon")
    public ResponseEntity<UserCouponCursorResponse> getUserCoupons(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest request,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "5") int size
    ){
        UserCouponCursorResponse responses = userCouponQueryService.getUserCoupons(request.userId(), cursorId, size);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/project")
    public ResponseEntity<MyProjectScrollResponse> getMyProjects(
            @CheckLogin LoginUserRequest loginUserRequest,
            @RequestParam(required = false) LocalDateTime nextCursorValue,
            @RequestParam(required = false) Long nextProjectId,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        MyProjectScrollRequest request = new MyProjectScrollRequest(nextCursorValue, nextProjectId, pageSize);
        return ResponseEntity.ok(myInfoService.getMyProjects(loginUserRequest.userId(), request));
    }


    @GetMapping("/order")
    public ResponseEntity<MyOrderScrollResponse> getMyOrders(
            @CheckLogin LoginUserRequest loginUserRequest,
            @RequestParam(required = false) LocalDateTime nextCursorValue,
            @RequestParam(required = false) Long nextOrderId,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        MyOrderScrollRequest request = new MyOrderScrollRequest(nextCursorValue, nextOrderId, pageSize);
        return ResponseEntity.ok(myInfoService.getMyOrders(loginUserRequest.userId(), request));
    }
}
