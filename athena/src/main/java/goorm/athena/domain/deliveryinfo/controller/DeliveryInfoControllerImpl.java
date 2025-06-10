package goorm.athena.domain.deliveryinfo.controller;

import goorm.athena.domain.deliveryinfo.dto.req.DeliveryChangeStateRequest;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoCommandService;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoQueryService;
import goorm.athena.global.jwt.util.CheckLogin;
import goorm.athena.global.jwt.util.LoginUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/delivery")
public class DeliveryInfoControllerImpl  implements DeliveryInfoController {

    private final DeliveryInfoQueryService deliveryInfoQueryService;
    private final DeliveryInfoCommandService deliveryInfoCommandService;

    @PostMapping("/delivery-info")
    public ResponseEntity<Void> addDeliveryInfo(
            @CheckLogin LoginUserRequest loginUser,
            @RequestBody DeliveryInfoRequest request
    ) {
        deliveryInfoCommandService.addDeliveryInfo(loginUser.userId(), request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/state")
    public ResponseEntity<Void> changeDeliveryInfoState(
            @CheckLogin LoginUserRequest loginUser,
            @RequestBody DeliveryChangeStateRequest request
    ) {
        deliveryInfoCommandService.changeDeliveryState(loginUser.userId(), request.deliveryInfoId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delivery-info/{id}")
    public ResponseEntity<Void> deleteDeliveryInfo(
            @CheckLogin LoginUserRequest loginUser,
            @PathVariable Long id
    ) {
        deliveryInfoCommandService.deleteDeliveryInfo(loginUser.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/delivery-info")
    public ResponseEntity<List<DeliveryInfoResponse>> getDeliveryInfoList(
            @CheckLogin LoginUserRequest loginUser
    ) {
        return ResponseEntity.ok(deliveryInfoQueryService.getMyDeliveryInfo(loginUser.userId()));
    }
}
