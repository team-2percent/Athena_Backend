package goorm.athena.domain.deliveryinfo.controller;

import goorm.athena.domain.deliveryinfo.DeliveryControllerIntegrationTestSupport;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryChangeStateRequest;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class DeliveryInfoControllerImplTest extends DeliveryControllerIntegrationTestSupport {

    @DisplayName("로그인 한 사용자가 자신의 배송 정보를 생성한다.")
    @Test
    void addDeliveryInfo() {
        // given
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        DeliveryInfoRequest request = new DeliveryInfoRequest("123", "123", "123");

        // when
        ResponseEntity<Void> response = controller.addDeliveryInfo(loginUserRequest, request);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(deliveryInfoService).addDeliveryInfo(loginUserRequest.userId(), request);
    }

    @DisplayName("유저의 기존에 저장된 배송 정보와 선택한 배송 정보의 상태를 변경한다.")
    @Test
    void changeDeliveryInfoState() {
        // given
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        DeliveryChangeStateRequest request = new DeliveryChangeStateRequest(1L);

        // when
        ResponseEntity<Void> response = controller.changeDeliveryInfoState(loginUserRequest, request);

        // then
        assertEquals(204, response.getStatusCodeValue());
        verify(deliveryInfoService).changeDeliveryState(loginUserRequest.userId(), request.deliveryInfoId());
    }

    @DisplayName("로그인 한 사용자가 자신의 선택한 배송 정보를 삭제한다.")
    @Test
    void deleteDeliveryInfo() {
        // given
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);
        Long id = 1L;

        // when
        ResponseEntity<Void> response = controller.deleteDeliveryInfo(loginUserRequest, id);

        // then
        assertEquals(204, response.getStatusCodeValue());
        verify(deliveryInfoService).deleteDeliveryInfo(loginUserRequest.userId(), id);
    }

    @DisplayName("로그인 한 사용자가 자신의 배송 정보들을 조회한다.")
    @Test
    void getDeliveryInfoList() {
        // given
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", 1L, Role.ROLE_USER);

        // when
        ResponseEntity<List<DeliveryInfoResponse>> response = controller.getDeliveryInfoList(loginUserRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        verify(deliveryInfoService).getMyDeliveryInfo(loginUserRequest.userId());
    }
}