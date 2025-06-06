package goorm.athena.domain.deliveryinfo.controller;

import goorm.athena.domain.deliveryinfo.DeliveryControllerIntegrationTestSupport;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryChangeStateRequest;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryInfoControllerImplTest extends DeliveryControllerIntegrationTestSupport {

    @DisplayName("로그인 한 사용자가 자신의 배송 정보를 생성한다.")
    @Test
    void addDeliveryInfo() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);
        DeliveryInfoRequest request = new DeliveryInfoRequest("서울시 강남구", "01012345678", "홍길동");

        // when
        ResponseEntity<Void> response = controller.addDeliveryInfo(loginUserRequest, request);

        // then
        assertEquals(200, response.getStatusCodeValue());

        List<DeliveryInfo> all = deliveryInfoRepository.findAll();
        assertEquals(1, all.size());
        assertEquals("서울시 강남구", all.get(0).getZipcode());
    }

    @DisplayName("유저의 기존에 저장된 배송 정보와 선택한 배송 정보의 상태를 변경한다.")
    @Test
    void changeDeliveryInfoState() {
        // given
        User user = setupUser("123123213", "123", "123", null);
        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "123", "123", "123", true);
        DeliveryInfo deliveryInfo2 = new DeliveryInfo(user, "123", "123", "123", false);

        userRepository.save(user);

        deliveryInfoRepository.saveAll(List.of(deliveryInfo, deliveryInfo2));

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);
        DeliveryChangeStateRequest request = new DeliveryChangeStateRequest(deliveryInfo2.getId());

        // when
        ResponseEntity<Void> response = controller.changeDeliveryInfoState(loginUserRequest, request);

        // then
        assertEquals(204, response.getStatusCodeValue());
        assertEquals(deliveryInfo2.getId(), deliveryInfoService.getPrimaryDeliveryInfo(user.getId()).getId());
    }

    @DisplayName("로그인 한 사용자가 자신의 선택한 배송 정보를 삭제한다.")
    @Test
    void deleteDeliveryInfo() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!23", "123", "123", false);
        DeliveryInfo deliveryInfo2 = new DeliveryInfo(user, "!234", "124", "1243", false);
        deliveryInfoRepository.saveAll(List.of(deliveryInfo, deliveryInfo2));

        // when
        ResponseEntity<Void> response = controller.deleteDeliveryInfo(loginUserRequest, deliveryInfo2.getId());

        // then
        assertEquals(204, response.getStatusCodeValue());

        boolean exists = deliveryInfoRepository.findById(deliveryInfo2.getId()).isPresent();
        assertThat(exists).isFalse();
    }

    @DisplayName("로그인 한 사용자가 자신의 배송 정보들을 조회한다.")
    @Test
    void getDeliveryInfoList() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);
        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);

        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!234", "124", "1243", false);
        DeliveryInfo deliveryInfo2 = new DeliveryInfo(user, "!234", "124", "1243", false);
        deliveryInfoRepository.save(deliveryInfo);
        deliveryInfoRepository.save(deliveryInfo2);

        // when
        ResponseEntity<List<DeliveryInfoResponse>> response = controller.getDeliveryInfoList(loginUserRequest);

        // then
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(response.getBody().size(), 2);
        assertEquals(response.getBody().get(1).zipcode(), "!234");
    }
}