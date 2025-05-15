package goorm.athena.domain.user.controller;

import goorm.athena.domain.comment.dto.res.CommentGetResponse;
import goorm.athena.domain.comment.service.CommentService;
import goorm.athena.domain.user.dto.request.UserPasswordRequest;
import goorm.athena.domain.user.dto.request.UserUpdatePasswordRequest;
import goorm.athena.domain.user.dto.response.UserGetResponse;
import goorm.athena.domain.user.dto.response.UserSummaryResponse;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.domain.userCoupon.dto.cursor.UserCouponCursorResponse;
import goorm.athena.domain.userCoupon.service.UserCouponService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class UserInfoControllerImpl implements UserInfoController {
    private final CommentService commentService;
    private final UserService userService;
    private final UserCouponService userCouponService;

    @Override
    @GetMapping("/info")
    public ResponseEntity<UserSummaryResponse> getSummary(@CheckLogin LoginUserRequest request){
        UserSummaryResponse response = userService.getUserSummary(request.userId());
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/comments")
    public List<CommentGetResponse> getComments(@CheckLogin LoginUserRequest request){
        return commentService.getCommentByUser(request.userId());
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
    @GetMapping("/coupons")
    public ResponseEntity<UserCouponCursorResponse> getUserCoupons(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest request,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "5") int size
    ){
        UserCouponCursorResponse responses = userCouponService.getUserCoupons(request.userId(), cursorId, size);
        return ResponseEntity.ok(responses);
    }
}
