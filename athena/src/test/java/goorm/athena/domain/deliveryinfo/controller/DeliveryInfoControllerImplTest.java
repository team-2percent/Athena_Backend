package goorm.athena.domain.deliveryinfo.controller;

import goorm.athena.domain.deliveryinfo.DeliveryControllerIntegrationTestSupport;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryChangeStateRequest;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.jwt.util.LoginUserRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryInfoControllerImplTest extends DeliveryControllerIntegrationTestSupport {

    @Transactional
    @DisplayName("로그인 한 사용자가 자신의 배송 정보를 생성한다.")
    @Test
    void addDeliveryInfo() {
        // given
        User user = setupUser("123", "123", "123", null);
        userRepository.save(user);

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);
        DeliveryInfoRequest request = new DeliveryInfoRequest("서울시 강남구", "01012345678", "홍길동");
        int size = deliveryInfoRepository.findAll().size();

        // when
        ResponseEntity<Void> response = controller.addDeliveryInfo(loginUserRequest, request);

        // then
        assertEquals(200, response.getStatusCodeValue());

        List<DeliveryInfo> all = deliveryInfoRepository.findAll();
        assertThat(size+1).isEqualTo(all.size());
        assertThat("서울시 강남구").isEqualTo(all.getLast().getZipcode());
    }

    @Transactional
    @DisplayName("유저의 기존에 저장된 배송 정보와 선택한 배송 정보의 상태를 변경한다.")
    @Test
    void changeDeliveryInfoState() {
        // given
        User user = setupUser("123123213", "123", "123", null);
        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "123", "123", "123", false);
        DeliveryInfo deliveryInfo2 = new DeliveryInfo(user, "123", "123", "123", false);

        userRepository.save(user);
        deliveryInfoRepository.saveAll(List.of(deliveryInfo, deliveryInfo2));

        LoginUserRequest loginUserRequest = new LoginUserRequest("123", user.getId(), Role.ROLE_USER);
        DeliveryChangeStateRequest request = new DeliveryChangeStateRequest(deliveryInfo2.getId());

        DeliveryInfo primaryDelivery = deliveryInfoQueryService.getPrimaryDeliveryInfo(user.getId());

        // when
        ResponseEntity<Void> response = controller.changeDeliveryInfoState(loginUserRequest, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(primaryDelivery.isDefault()).isFalse();
        assertThat(deliveryInfo2.isDefault()).isTrue();
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
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

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

        int size = deliveryInfoRepository.findByUserId(user.getId()).size();

        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!2343", "124", "1243", false);
        DeliveryInfo deliveryInfo2 = new DeliveryInfo(user, "!234", "124", "1243", false);
        deliveryInfoRepository.save(deliveryInfo);
        deliveryInfoRepository.save(deliveryInfo2);

        // when
        ResponseEntity<List<DeliveryInfoResponse>> response = controller.getDeliveryInfoList(loginUserRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(size+2);
        assertThat(response.getBody().get(size).zipcode()).isEqualTo(deliveryInfo.getZipcode());
    }
}