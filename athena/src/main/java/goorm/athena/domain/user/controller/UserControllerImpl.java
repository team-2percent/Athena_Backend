package goorm.athena.domain.user.controller;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserControllerImpl implements UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final ImageGroupService imageGroupService;
    private final FcmTokenService fcmTokenService;

    @Override
    @PostMapping
    public ResponseEntity<UserCreateResponse> createUser(@RequestBody UserCreateRequest request) {
        // User <-> ImageGroup 1:1 매핑되도록 생성
        ImageGroup userImageGroup = imageGroupService.createImageGroup(Type.USER);
        UserCreateResponse response = userService.createUser(request, userImageGroup);
        return ResponseEntity.ok(response);
    }

    // userId와 업데이트 dto를 따로 관리하는 이유는, '@CheckLogin'에서 LoginUserRequest를 매개변수로 받으면
    // jwt 토큰 검증을 실시합니다.
    @Override
    @PutMapping
    public ResponseEntity<UserUpdateResponse> updateUser(@CheckLogin LoginUserRequest loginUserRequest,
                                                         @RequestPart (value = "request") UserUpdateRequest request,
                                                         @RequestPart(value = "files", required = false) MultipartFile file){

        UserUpdateResponse response = userService.updateUser(loginUserRequest.userId(), request, file);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserGetResponse> getUserById(@PathVariable Long id) {
        UserGetResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request, BindingResult bindingResult, HttpServletResponse response){
        if(bindingResult.hasErrors()){
            throw new CustomException(ErrorCode.VALIDATION_ERROR);
        }

        UserLoginResponse loginResponse = userService.validateUserCredentials(request, response);

        return ResponseEntity.ok(loginResponse);
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CheckLogin LoginUserRequest request,
        //    @CookieValue("refreshToken") String refreshToken,
                                       HttpServletResponse response) {
        /*
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new CustomException(ErrorCode.REFRESHTOKEN_NOT_FOUND);
        }
        */

        // refreshTokenService.deleteRefreshToken(response);
        fcmTokenService.deleteToken(request.userId());      // 특정 유저 FCM 토큰 삭제

        return ResponseEntity.ok().build();
    }


    @Override
    @GetMapping("/Header")
    public ResponseEntity<UserHeaderGetResponse> getHeader(@CheckLogin LoginUserRequest request){
        UserHeaderGetResponse response = userService.getHeaderById(request.userId());
        return ResponseEntity.ok(response);
    }


    @Override
    @GetMapping("/profile/{id}")
    public ResponseEntity<UserGetResponse> getUserProfile(@PathVariable Long id) {
        UserGetResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }
}
