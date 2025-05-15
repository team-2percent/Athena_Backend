package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.response.MyOrderScrollRequest;
import goorm.athena.domain.user.dto.response.MyOrderScrollResponse;
import goorm.athena.domain.user.dto.response.MyProjectScrollRequest;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import goorm.athena.domain.user.service.MyInfoService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/my")
public class UserInfoControllerImpl implements UserInfoController{

    private final MyInfoService myInfoService;

    @GetMapping("/projects")
    public ResponseEntity<MyProjectScrollResponse> getMyProjects(
            @CheckLogin LoginUserRequest loginUserRequest,
            @RequestParam(required = false) LocalDateTime nextCursorValue,
            @RequestParam(required = false) Long nextProjectId,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        MyProjectScrollRequest request = new MyProjectScrollRequest(nextCursorValue, nextProjectId, pageSize);
        return ResponseEntity.ok(myInfoService.getMyProjects(loginUserRequest.userId(), request));
    }


    @GetMapping("/orders")
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
