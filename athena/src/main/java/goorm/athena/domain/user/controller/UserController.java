package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.request.UserUpdateRequest;
import goorm.athena.domain.user.dto.response.UserCreateResponse;
import goorm.athena.domain.user.dto.response.UserUpdateResponse;
import goorm.athena.domain.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
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
            "password는 암호화되어 처리되며 패스워드 확인 검증없이 입력된 정보로 곧바로 수정로직을 진행합니다.")
    @ApiResponse(responseCode = "200", description = "유저 정보 수정 성공")
    @PutMapping
    ResponseEntity<UserUpdateResponse> updateUser(@RequestBody UserUpdateRequest request);
}
