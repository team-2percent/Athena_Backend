package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.UserLoginResponse;
import goorm.athena.domain.user.dto.response.UserCreateResponse;
import goorm.athena.domain.user.dto.response.UserGetResponse;
import goorm.athena.domain.user.dto.response.UserUpdateResponse;
import goorm.athena.domain.user.service.RefreshTokenService;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserControllerImpl implements UserController{

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @PostMapping
    public ResponseEntity<UserCreateResponse> createUser(@RequestBody UserCreateRequest request){
        UserCreateResponse response = userService.createUser(request);
        return ResponseEntity.ok(response);
    }

    // userId와 업데이트 dto를 따로 관리하는 이유는, '@CheckLogin'에서 LoginUserRequest를 매개변수로 받으면
    // jwt 토큰 검증을 실시합니다.
    @Override
    @PutMapping
    public ResponseEntity<UserUpdateResponse> updateUser(@CheckLogin LoginUserRequest loginUserRequest,
                                                         @RequestBody UserUpdateRequest request){
        UserUpdateResponse response = userService.updateUser(loginUserRequest.userId(), request);
        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserGetResponse> getUserById(@PathVariable Long id){
        UserGetResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
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
    public ResponseEntity<String> logout(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new CustomException(ErrorCode.REFRESHTOKEN_NOT_FOUND);
        }

        refreshTokenService.deleteRefreshToken(refreshToken);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cooke", cookie.toString());

        return ResponseEntity.ok("Logged out");
    }
}
