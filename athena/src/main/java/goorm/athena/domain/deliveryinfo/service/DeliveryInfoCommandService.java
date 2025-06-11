package goorm.athena.domain.deliveryinfo.service;

import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.deliveryinfo.mapper.DeliveryInfoMapper;
import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryInfoCommandService {
    private final DeliveryInfoRepository deliveryInfoRepository;
    private final UserQueryService userQueryService;
    private final DeliveryInfoQueryService deliveryInfoQueryService;
    private final DeliveryInfoMapper deliveryInfoMapper;

    @Transactional
    public void addDeliveryInfo(Long userId, DeliveryInfoRequest request) {
        User user = userQueryService.getUser(userId);

        boolean isDefault = !hasPrimaryDeliveryInfo(userId);
        DeliveryInfo info = deliveryInfoMapper.toEntity(
                user,
                request.zipcode(),
                request.address(),
                request.detailAddress(),
                isDefault
        );

        deliveryInfoRepository.save(info);
    }

    @Transactional
    public void changeDeliveryState(Long userId, Long deliveryInfoId){
        DeliveryInfo previousDeliveryInfo = deliveryInfoQueryService.getPrimaryDeliveryInfo(userId);
        DeliveryInfo newDeliveryInfo = deliveryInfoQueryService.getById(deliveryInfoId);

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
    public void deleteDeliveryInfo(Long userId, Long id) {
        DeliveryInfo info = deliveryInfoQueryService.getById(id);

        if (!info.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 기본 배송지면 삭제 제한
        if (info.isDefault()) {
            throw new CustomException(ErrorCode.BASIC_DELIVERY_NOT_DELETED);
        }

        deliveryInfoRepository.delete(info);
    }

    public boolean hasPrimaryDeliveryInfo(Long userId) {
        return deliveryInfoRepository.existsByUserIdAndIsDefaultTrue(userId);
    }
}
