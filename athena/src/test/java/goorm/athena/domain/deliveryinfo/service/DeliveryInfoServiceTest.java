package goorm.athena.domain.deliveryinfo.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.deliveryinfo.DeliveryIntegrationTestSupport;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class DeliveryInfoServiceTest extends DeliveryIntegrationTestSupport {

    @DisplayName("유저가 존재하지 않는 배송 정보를 조회하면 에러를 리턴한다.")
    @Test
    void getById_Error() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);
        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!23", "123", "123", true);
        deliveryInfoRepository.save(deliveryInfo);

        // when & then
        assertThatThrownBy(() -> deliveryInfoService.getById(100000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DELIVERY_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 배송 정보를 저장했다면 배송 정보의 ID로 자신의 해당 정보를 조회한다.")
    @Test
    void getById() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);
        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!23", "123", "123", true);
        deliveryInfoRepository.save(deliveryInfo);

        // when
        DeliveryInfo found = deliveryInfoService.getById(deliveryInfo.getId());

        // then
        assertThat(found.getId()).isEqualTo(deliveryInfo.getId());
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
    }


    @DisplayName("유저가 자신의 배송 정보를 입력해 저장한다.")
    @Test
    void addDeliveryInfo_Primary() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);

        DeliveryInfo userDelivery = deliveryInfoService.getPrimaryDeliveryInfo(user.getId());
        userDelivery.unsetAsDefault();
        deliveryInfoRepository.save(userDelivery);
        DeliveryInfoRequest request = new DeliveryInfoRequest("홍길동", "서울시", "010-1111-2222");

        int size = deliveryInfoRepository.findByUserId(user.getId()).size();

        // when
        deliveryInfoService.addDeliveryInfo(user.getId(), request);

        // then
        List<DeliveryInfo> infos = deliveryInfoRepository.findByUserId(user.getId());
        assertThat(infos).hasSize(size+1);
        assertThat(infos.getLast().isDefault()).isTrue(); // 첫 배송지는 기본 배송지로 설정됨
        assertThat(userDelivery.isDefault()).isFalse();
    }


    @DisplayName("기본 배송 정보를 조회할 시 해당 정보가 존재하지 않을 경우 에러를 리턴한다.")
    @Test
    void getPrimaryDeliveryInfo_ThrowsDeliveryNotFound() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("user_no_delivery", "abc", "abc", imageGroup);
        userRepository.save(user);

        // 기본 배송지 없음 상태

        // when & then
        assertThatThrownBy(() -> deliveryInfoService.getPrimaryDeliveryInfo(100000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DELIVERY_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 이미 기존 배송 정보를 저장했었다면 현재 배송 정보는 일반 배송 정보로 저장한다.")
    @Test
    void addDeliveryInfo_Normal() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);
        DeliveryInfoRequest request = new DeliveryInfoRequest("홍길동", "서울시", "010-1111-2222");
        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!23", "123", "123", false);
        deliveryInfoRepository.save(deliveryInfo);

        int size = deliveryInfoRepository.findByUserId(user.getId()).size();

        // when
        deliveryInfoService.addDeliveryInfo(user.getId(), request);

        // then
        List<DeliveryInfo> response = deliveryInfoRepository.findByUserId(user.getId());
        assertThat(response).hasSize(size+1);
        assertThat(response.get(size).isDefault()).isFalse(); // 두 번쨰 배송지부터 일반 배송지로 설정됨
    }

    @DisplayName("유저가 배송 정보를 변경할 때 다른 유저의 배송 정보를 변경하면 NOT_FOUND 에러를 리턴한다.")
    @Test
    void changeDeliveryState_NotMyUser() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        ImageGroup imageGroup2 = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        User user2 = setupUser("1234", "123", "123", imageGroup2);
        userRepository.saveAll(List.of(user, user2));

        DeliveryInfo oldDeliveryInfo = new DeliveryInfo(user, "!23", "123", "123", false);
        DeliveryInfo newDeliveryInfo = new DeliveryInfo(user2, "!234", "1243", "1234", false);
        deliveryInfoRepository.saveAll(List.of(oldDeliveryInfo, newDeliveryInfo));

        // when & then
        assertThatThrownBy(() -> deliveryInfoService.changeDeliveryState(user.getId(), newDeliveryInfo.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DELIVERY_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 기존 배송 정보를 기존 배송 정보로 변경하려고 하면 ALREADY_DEFAULT 에러를 리턴한다.")
    @Test
    void changeDeliveryState_ALREADY() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);

        DeliveryInfo deliveryInfo = deliveryInfoService.getPrimaryDeliveryInfo(user.getId());

  //      DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!23", "123", "123", false);
  //      deliveryInfoRepository.save(deliveryInfo);

        // when & then
        assertThatThrownBy(() -> deliveryInfoService.changeDeliveryState(user.getId(), deliveryInfo.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_DEFAULT_DELIVERY.getErrorMessage());
    }

    @DisplayName("현재의 배송 정보를 기본 배송 정보로 변경하고, 이전 배송 정보는 일반 배송 정보로 변경한다.")
    @Test
    void changeDeliveryState() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);

        DeliveryInfo oldDeliveryInfo = new DeliveryInfo(user, "!23", "123", "123", false);
        DeliveryInfo newDeliveryInfo = new DeliveryInfo(user, "!234", "1243", "1234", false);
        deliveryInfoRepository.saveAll(List.of(oldDeliveryInfo, newDeliveryInfo));

        DeliveryInfo primaryDeliveryInfo = deliveryInfoService.getPrimaryDeliveryInfo(user.getId());

        // when
        deliveryInfoService.changeDeliveryState(user.getId(), newDeliveryInfo.getId());

   //     DeliveryInfo updatedOld = deliveryInfoRepository.findById(oldDeliveryInfo.getId()).orElseThrow();
        DeliveryInfo updatedNew = deliveryInfoRepository.findById(newDeliveryInfo.getId()).orElseThrow();

        // then
        assertThat(primaryDeliveryInfo.isDefault()).isFalse();
        assertThat(updatedNew.isDefault()).isTrue();
    }

    @DisplayName("유저가 다른 사람의 배송 정보를 삭제하고자 하면 접근 거부 에러를 리턴한다.")
    @Test
    void deleteDeliveryInfo_NotMyUser() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);
        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!23", "123", "123", true);
        deliveryInfoRepository.save(deliveryInfo);

        // when & then
        assertThatThrownBy(() -> deliveryInfoService.deleteDeliveryInfo(99L, deliveryInfo.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getErrorMessage());
    }

    @DisplayName("유저가 기본 배송 정보를 삭제하고자 하면 에러를 리턴한다.")
    @Test
    void deleteDeliveryInfo_Primary() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);
        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!23", "123", "123", true);
        deliveryInfoRepository.save(deliveryInfo);

        // when & then
        assertThatThrownBy(() -> deliveryInfoService.deleteDeliveryInfo(user.getId(), deliveryInfo.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BASIC_DELIVERY_NOT_DELETED.getErrorMessage());
    }

    @DisplayName("유저의 일반 배송 정보를 삭제한다.")
    @Test
    void deleteDeliveryInfo_Normal() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);
        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!23", "123", "123", true);
        DeliveryInfo deliveryInfo2 = new DeliveryInfo(user, "!234", "124", "1243", false);
        deliveryInfoRepository.saveAll(List.of(deliveryInfo, deliveryInfo2));

        // when
        deliveryInfoService.deleteDeliveryInfo(user.getId(), deliveryInfo2.getId());

        // then
        boolean exists = deliveryInfoRepository.findById(deliveryInfo2.getId()).isPresent();
        assertThat(exists).isFalse();
    }

    @DisplayName("유저가 자신이 등록한 배송 정보들을 조회한다.")
    @Test
    void getMyDeliveryInfo() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);

        int size = deliveryInfoRepository.findByUserId(user.getId()).size();

        DeliveryInfo oldDeliveryInfo = new DeliveryInfo(user, "!23", "123", "123", false);
        DeliveryInfo newDeliveryInfo = new DeliveryInfo(user, "!234", "1243", "1234", false);
        deliveryInfoRepository.saveAll(List.of(oldDeliveryInfo, newDeliveryInfo));


        // when
        List<DeliveryInfoResponse> responses = deliveryInfoService.getMyDeliveryInfo(user.getId());

        // then
        assertThat(responses).hasSize(size+2);
        assertThat(responses.get(size).zipcode()).isEqualTo("!23");
        assertThat(responses.get(size+1).zipcode()).isEqualTo("!234");
    }

    @DisplayName("유저가 자신이 등록한 기본 배송 정보를 조회한다.")
    @Test
    void getPrimaryDeliveryInfo() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("123", "123", "123", imageGroup);
        userRepository.save(user);

        DeliveryInfo oldDeliveryInfo = new DeliveryInfo(user, "!23", "123", "123", false);
        DeliveryInfo newDeliveryInfo = new DeliveryInfo(user, "!234", "1243", "1234", false);
        deliveryInfoRepository.saveAll(List.of(oldDeliveryInfo, newDeliveryInfo));

        // when
        DeliveryInfo response = deliveryInfoService.getPrimaryDeliveryInfo(user.getId());

        // then
        assertThat(response.isDefault()).isTrue();
    }
}