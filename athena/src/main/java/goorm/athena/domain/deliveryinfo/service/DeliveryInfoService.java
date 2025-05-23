package goorm.athena.domain.deliveryinfo.service;

import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoUpdateRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DeliveryInfoService {

    private final DeliveryInfoRepository deliveryInfoRepository;
    private final UserService userService;

    public DeliveryInfo getById(Long id) {
        return deliveryInfoRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));
    }

    @Transactional
    public void addDeliveryInfo(Long userId, DeliveryInfoRequest request) {
        User user = userService.getUser(userId);

        boolean isDefault;
        try {
            // 기본 배송지 존재 여부 확인
            getPrimaryDeliveryInfo(userId);
            isDefault = false;
        } catch (CustomException e) {
            if (e.getErrorCode() == ErrorCode.DELIVERY_NOT_FOUND) {
                isDefault = true;
            } else {
                throw e;
            }
        }

        DeliveryInfo info = DeliveryInfo.of(user, request, isDefault);
        deliveryInfoRepository.save(info);
    }

    @Transactional
    public void changeDeliveryState(Long userId, Long deliveryInfoId){
        DeliveryInfo previousDeliveryInfo = getPrimaryDeliveryInfo(userId);
        DeliveryInfo newDeliveryInfo = getById(deliveryInfoId);

        if(!newDeliveryInfo.getUser().getId().equals(userId)){
            throw new CustomException(ErrorCode.DELIVERY_NOT_FOUND);
        }

        if(previousDeliveryInfo.getId().equals(deliveryInfoId)){
            throw new CustomException(ErrorCode.ALREADY_DEFAULT_DELIVERY);
        }

        previousDeliveryInfo.unsetAsDefault();
        newDeliveryInfo.setAsDefault();

        deliveryInfoRepository.saveAll(List.of(previousDeliveryInfo, newDeliveryInfo));
    }

    @Transactional
    public void updateDeliveryInfo(Long userId, Long id, DeliveryInfoUpdateRequest request) {
        User user = userService.getUser(userId);
        DeliveryInfo info = deliveryInfoRepository.findById(id).orElseThrow();
        if (!info.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        info.update(request.zipcode(), request.address(), request.detailAddress());
    }

    @Transactional
    public void deleteDeliveryInfo(Long userId, Long id) {
        DeliveryInfo info = getById(id);

        if (!info.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 기본 배송지면 삭제 제한
        if (info.isDefault()) {
            throw new CustomException(ErrorCode.BASIC_ACCOUNT_NOT_DELETED);
        }

        deliveryInfoRepository.delete(info);
    }

    @Transactional
    public void setDefault(Long userId, Long id) {
        User user = userService.getUser(userId);

        DeliveryInfo newDefault = deliveryInfoRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));

        if (!newDefault.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (newDefault.isDefault()) {
            throw new CustomException(ErrorCode.ALREADY_DEFAULT_DELIVERY);
        }

        // 현재 기본 배송지가 있다면 false로 변경
        deliveryInfoRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(currentDefault -> currentDefault.updateDefault(false));

        newDefault.updateDefault(true);
    }

    public List<DeliveryInfoResponse> getMyDeliveryInfo(Long userId) {
        return deliveryInfoRepository.findByUserId(userId).stream()
                .map(DeliveryInfoResponse::from)
                .collect(Collectors.toList());
    }

    private void unsetPreviousDefault(User user) {
        List<DeliveryInfo> infos = deliveryInfoRepository.findByUserId(user.getId());
        for (DeliveryInfo info : infos) {
            if (info.isDefault()) {
                info.updateDefault(false);
            }
        }
    }

    public DeliveryInfo getPrimaryDeliveryInfo(Long userId) {
        return deliveryInfoRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));
    }

}
