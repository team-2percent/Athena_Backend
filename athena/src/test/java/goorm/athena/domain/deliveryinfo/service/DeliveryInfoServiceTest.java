package goorm.athena.domain.deliveryinfo.service;

import goorm.athena.domain.deliveryinfo.DeliveryIntegrationTestSupport;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeliveryInfoServiceTest extends DeliveryIntegrationTestSupport {

    @DisplayName("유저가 존재하지 않는 배송 정보를 조회하면 에러를 리턴한다.")
    @Test
    void getById_Error() {
        // given
        User user = userRepository.findById(12L).get();

        // when & then
        assertThatThrownBy(() -> deliveryInfoQueryService.getById(100000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DELIVERY_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 배송 정보를 저장했다면 배송 정보의 ID로 자신의 해당 정보를 조회한다.")
    @Test
    void getById() {
        // given
        User user = userRepository.findById(12L).get();

        // when
        DeliveryInfo found = deliveryInfoQueryService.getById(12L);

        // then
        assertThat(found.getId()).isEqualTo(12L);
        assertThat(found.getUser().getId()).isEqualTo(user.getId());
    }


    @DisplayName("유저가 자신의 배송 정보를 입력해 저장한다.")
    @Test
    void addDeliveryInfo_Primary() {
        // given
        User user = userRepository.findById(4L).get();

        DeliveryInfoRequest request = new DeliveryInfoRequest("홍길동", "서울시", "010-1111-2222");

        int size = deliveryInfoRepository.findByUserId(user.getId()).size();

        // when
        deliveryInfoCommandService.addDeliveryInfo(user.getId(), request);

        // then
        List<DeliveryInfo> infos = deliveryInfoRepository.findByUserId(user.getId());
        assertThat(infos).hasSize(size+1);
        assertThat(infos.getLast().isDefault()).isTrue();
    }


    @DisplayName("기본 배송 정보를 조회할 시 해당 정보가 존재하지 않을 경우 에러를 리턴한다.")
    @Test
    void getPrimaryDeliveryInfo_ThrowsDeliveryNotFound() {
        // given
        User user = userRepository.findById(12L).get();

        // 기본 배송지 없음 상태

        // when & then
        assertThatThrownBy(() -> deliveryInfoQueryService.getPrimaryDeliveryInfo(100000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DELIVERY_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 이미 기존 배송 정보를 저장했었다면 현재 배송 정보는 일반 배송 정보로 저장한다.")
    @Test
    void addDeliveryInfo_Normal() {
        // given
        User user = userRepository.findById(5L).get();
        DeliveryInfoRequest request = new DeliveryInfoRequest("홍길동", "서울시", "010-1111-2222");

        // when
        deliveryInfoCommandService.addDeliveryInfo(user.getId(), request);

        DeliveryInfo newDeliveryInfo = deliveryInfoRepository.findById(32L).get();

        // then
        List<DeliveryInfo> response = deliveryInfoRepository.findByUserId(user.getId());
        assertThat(newDeliveryInfo.isDefault()).isFalse();
    }

    @DisplayName("유저가 배송 정보를 변경할 때 다른 유저의 배송 정보를 변경하면 NOT_FOUND 에러를 리턴한다.")
    @Test
    void changeDeliveryState_NotMyUser() {
        // given
        User user = userRepository.findById(11L).get();
        User user2 = userRepository.findById(12L).get();

        DeliveryInfo newDeliveryInfo = deliveryInfoRepository.findByUserIdAndIsDefaultTrue(user2.getId()).get();

        // when & then
        assertThatThrownBy(() -> deliveryInfoCommandService.changeDeliveryState(user.getId(), newDeliveryInfo.getId()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DELIVERY_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 배송 정보를 변경할 때 다른 유저의 배송 정보를 변경하면 NOT_FOUND 에러를 리턴한다.")
    @Test
    void changeDeliveryState_Delivery_Not_Found() {
        // given

        // when & then
        assertThatThrownBy(() -> deliveryInfoCommandService.changeDeliveryState(51L, 16L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.DELIVERY_NOT_FOUND.getErrorMessage());
    }

    @DisplayName("유저가 기존 배송 정보를 기존 배송 정보로 변경하려고 하면 ALREADY_DEFAULT 에러를 리턴한다.")
    @Test
    void changeDeliveryState_ALREADY() {
        // given
        User user = userRepository.findById(7L).get();

        // when & then
        assertThatThrownBy(() -> deliveryInfoCommandService.changeDeliveryState(user.getId(), 7L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ALREADY_DEFAULT_DELIVERY.getErrorMessage());
    }

    @DisplayName("현재의 배송 정보를 기본 배송 정보로 변경하고, 이전 배송 정보는 일반 배송 정보로 변경한다.")
    @Test
    void changeDeliveryState() {
        // given
        User user = userRepository.findById(3L).get();

        DeliveryInfo primaryDeliveryInfo = deliveryInfoQueryService.getPrimaryDeliveryInfo(user.getId());

        // when
        deliveryInfoCommandService.changeDeliveryState(user.getId(), 31L);
        DeliveryInfo updatedNew = deliveryInfoRepository.findById(31L).get();

        // then
        assertThat(primaryDeliveryInfo.isDefault()).isFalse();
        assertThat(updatedNew.isDefault()).isTrue();
    }

    @DisplayName("유저가 다른 사람의 배송 정보를 삭제하고자 하면 접근 거부 에러를 리턴한다.")
    @Test
    void deleteDeliveryInfo_NotMyUser() {
        // given
        User user = userRepository.findById(12L).get();

        // when & then
        assertThatThrownBy(() -> deliveryInfoCommandService.deleteDeliveryInfo(user.getId(), 26L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ACCESS_DENIED.getErrorMessage());
    }

    @DisplayName("유저가 기본 배송 정보를 삭제하고자 하면 에러를 리턴한다.")
    @Test
    void deleteDeliveryInfo_Primary() {
        // given
        User user = userRepository.findById(13L).get();

        // when & then
        assertThatThrownBy(() -> deliveryInfoCommandService.deleteDeliveryInfo(user.getId(), 13L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.BASIC_DELIVERY_NOT_DELETED.getErrorMessage());
    }

    @DisplayName("유저의 일반 배송 정보를 삭제한다.")
    @Test
    void deleteDeliveryInfo_Normal() {
        // given
        User user = userRepository.findById(13L).get();
        DeliveryInfo deliveryInfo = new DeliveryInfo(user, "!23", "123", "123", false);
        DeliveryInfo deliveryInfo2 = new DeliveryInfo(user, "!234", "124", "1243", false);
        deliveryInfoRepository.saveAll(List.of(deliveryInfo, deliveryInfo2));

        // when
        deliveryInfoCommandService.deleteDeliveryInfo(user.getId(), deliveryInfo2.getId());

        // then
        boolean exists = deliveryInfoRepository.findById(deliveryInfo2.getId()).isPresent();
        assertThat(exists).isFalse();
    }

    @DisplayName("유저가 자신이 등록한 배송 정보들을 조회한다.")
    @Test
    void getMyDeliveryInfo() {
        // given
        User user = userRepository.findById(14L).get();

        DeliveryInfo oldDeliveryInfo = new DeliveryInfo(user, "!23", "123", "123", false);
        DeliveryInfo newDeliveryInfo = new DeliveryInfo(user, "!234", "1243", "1234", false);
        deliveryInfoRepository.saveAll(List.of(oldDeliveryInfo, newDeliveryInfo));

        // when
        List<DeliveryInfoResponse> responses = deliveryInfoQueryService.getMyDeliveryInfo(user.getId());

        // then
        assertThat(responses).hasSize(3);
        assertThat(responses.get(1).zipcode()).isEqualTo("!23");
        assertThat(responses.get(2).zipcode()).isEqualTo("!234");
    }

    @DisplayName("유저가 자신이 등록한 기본 배송 정보를 조회한다.")
    @Test
    void getPrimaryDeliveryInfo() {
        // given
        User user = userRepository.findById(1L).get();

        // when
        DeliveryInfo response = deliveryInfoQueryService.getPrimaryDeliveryInfo(user.getId());

        // then
        assertThat(response.isDefault()).isTrue();
    }
}