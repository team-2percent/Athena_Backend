package goorm.athena.domain.user.controller;

import goorm.athena.domain.user.dto.response.MyOrderScrollResponse;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @GetMapping("/api/my/projects")
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
    @GetMapping("/api/my/orders")
    ResponseEntity<MyOrderScrollResponse> getMyOrders(
            @Parameter(hidden = true) @CheckLogin LoginUserRequest loginUserRequest,
            @Parameter(description = "커서: 마지막 항목의 orderedAt", example = "2025-05-14T15:30:00")
            @RequestParam(required = false) LocalDateTime nextCursorValue,
            @Parameter(description = "커서: 마지막 항목의 orderId", example = "456")
            @RequestParam(required = false) Long nextOrderId,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int pageSize
    );


}