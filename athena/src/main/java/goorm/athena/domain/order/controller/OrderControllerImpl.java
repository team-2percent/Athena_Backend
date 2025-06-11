package goorm.athena.domain.order.controller;

import goorm.athena.domain.order.dto.req.OrderCreateRequest;
import goorm.athena.domain.order.dto.res.OrderCreateResponse;
import goorm.athena.domain.order.service.OrderCommendService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderControllerImpl implements OrderController {

    private final OrderCommendService orderCommendService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(
            @CheckLogin LoginUserRequest loginUserRequest,
            @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderCommendService.createOrder(loginUserRequest.userId(), request);
        return ResponseEntity.ok(response);
    }
}