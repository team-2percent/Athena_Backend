package goorm.athena.domain.order.controller;

import goorm.athena.domain.order.dto.req.OrderCreateRequest;
import goorm.athena.domain.order.dto.res.OrderCreateResponse;
import goorm.athena.domain.order.service.OrderService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderControllerImpl implements OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderCreateResponse> createOrder(
            @CheckLogin LoginUserRequest loginUserRequest,
            @RequestBody OrderCreateRequest request
    ) {
        OrderCreateResponse response = orderService.createOrder(loginUserRequest.userId(), request);
        return ResponseEntity.ok(response);
    }
}