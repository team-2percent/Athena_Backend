package goorm.athena.domain.deliveryinfo.controller;

import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoUpdateRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.service.DeliveryInfoService;
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

    private final DeliveryInfoService deliveryInfoService;

    @PostMapping("/delivery-info")
    public ResponseEntity<Void> addDeliveryInfo(
            @CheckLogin LoginUserRequest loginUser,
            @RequestBody DeliveryInfoRequest request
    ) {
        deliveryInfoService.addDeliveryInfo(loginUser.userId(), request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/delivery-info/{id}")
    public ResponseEntity<Void> updateDeliveryInfo(
            @CheckLogin LoginUserRequest loginUser,
            @PathVariable Long id,
            @RequestBody DeliveryInfoUpdateRequest request
    ) {
        deliveryInfoService.updateDeliveryInfo(loginUser.userId(), id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delivery-info/{id}")
    public ResponseEntity<Void> deleteDeliveryInfo(
            @CheckLogin LoginUserRequest loginUser,
            @PathVariable Long id
    ) {
        deliveryInfoService.deleteDeliveryInfo(loginUser.userId(), id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/delivery-info/{id}/default")
    public ResponseEntity<Void> setDefaultDeliveryInfo(
            @CheckLogin LoginUserRequest loginUser,
            @PathVariable Long id
    ) {
        deliveryInfoService.setDefault(loginUser.userId(), id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/delivery-info")
    public ResponseEntity<List<DeliveryInfoResponse>> getDeliveryInfoList(
            @CheckLogin LoginUserRequest loginUser
    ) {
        return ResponseEntity.ok(deliveryInfoService.getMyDeliveryInfo(loginUser.userId()));
    }
}
