package goorm.athena.domain.order.controller;


import goorm.athena.domain.order.dto.req.OrderCreateRequest;
import goorm.athena.domain.order.dto.res.OrderCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/orders")
public interface OrderController {

    @Operation(
            summary = "주문 생성",
            description = "사용자가 주문 버튼을 누른경우 해당 api에 사용자가 선택한 상품 목록으로 주문을 요청 합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 생성 성공",
                    content = @Content(schema = @Schema(implementation = OrderCreateResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
    })
    @PostMapping("/{userId}")
    ResponseEntity<OrderCreateResponse> createOrder(
            @Parameter(description = "주문자 ID", required = true) @PathVariable Long userId,
            @RequestBody OrderCreateRequest request
    );
}