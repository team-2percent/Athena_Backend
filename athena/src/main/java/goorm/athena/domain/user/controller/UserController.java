package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserLoginRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.UserCreateResponse;
import goorm.athena.domain.user.dto.response.UserGetResponse;
import goorm.athena.domain.user.dto.response.UserLoginResponse;
import goorm.athena.domain.user.dto.response.UserUpdateResponse;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 관련 API")
@RequestMapping("/api/user")
public interface UserController {

    @Operation(summary = "유저 생성 API", description = "입력된 정보로 유저를 생성합니다.<br>" +
            "권한은 기본적으로 'USER'가 되며, 필요한 정보는 email, password, nickname입니다.<br>" +
            "password는 암호화되어 처리됩니다." )
    @ApiResponse(responseCode = "200", description = "새 유저 정보 생성 성공",
        content = @Content(schema = @Schema(implementation = User.class)))
    @PostMapping
    ResponseEntity<UserCreateResponse> createUser(@RequestBody UserCreateRequest request);

    @Operation(summary = "유저 수정 API", description = "입력된 정보로 유저의 정보를 수정합니다.<br>" +
            "필요한 정보는 email, password, nickname입니다.<br>" +
            "password는 암호화되어 처리되며 패스워드 확인 검증없이 입력된 정보로 곧바로 수정로직을 진행합니다.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "유저 정보 수정 성공")
    @PutMapping
    ResponseEntity<UserUpdateResponse> updateUser(@CheckLogin LoginUserRequest loginUserRequest,
                                                  @RequestBody UserUpdateRequest request);

    @Operation(summary = "유저 조회 APi", description = "유저의 ID를 통해 특정 유저의 정보를 조회합니다.<br>")
    @ApiResponse(responseCode = "200", description = "특정 유저 정보 조회 성공")
    @GetMapping("/{id}")
    ResponseEntity<UserGetResponse> getUserById(@PathVariable Long id);

    @Operation(summary = "유저 삭제 APi", description = "유저의 ID를 통해 특정 유저의 정보를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "특정 유저 정보 삭제 성공")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id);

    @Operation(summary = "유저 로그인 API", description = "입력된 유저의 아이디, 비밀번호를 통해 로그인을 진행합니다.")
    @ApiResponse(responseCode = "200", description = "유저 로그인 성공")
    @ApiResponse(responseCode = "401", description = "로그인 실패 - 잘못된 인증 정보",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/login")
    ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest loginDto, BindingResult bindingResult, HttpServletResponse response);

    @Operation(summary = "유저 로그아웃 API", description = "로그인 된 유저의 refreshToken을 서버에 저장된 쿠키에서 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "유저 로그아웃 성공")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response);
}
