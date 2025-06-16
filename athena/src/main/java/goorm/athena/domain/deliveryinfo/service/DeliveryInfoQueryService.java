package goorm.athena.domain.deliveryinfo.service;

import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.deliveryinfo.mapper.DeliveryInfoMapper;
import goorm.athena.domain.deliveryinfo.repository.DeliveryInfoRepository;
import goorm.athena.domain.user.service.UserQueryService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class DeliveryInfoQueryService {

    private final DeliveryInfoRepository deliveryInfoRepository;
    private final DeliveryInfoMapper deliveryInfoMapper;

    public DeliveryInfo getById(Long id) {
        return deliveryInfoRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));
    }

    public List<DeliveryInfoResponse> getMyDeliveryInfo(Long userId) {
        List<DeliveryInfo> deliveryInfos = deliveryInfoRepository.findByUserId(userId);
        return deliveryInfoMapper.toGetResponse(deliveryInfos);
    }

    public DeliveryInfo getPrimaryDeliveryInfo(Long userId) {
        return deliveryInfoRepository.findByUserIdAndIsDefaultTrue(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.DELIVERY_NOT_FOUND));
    }
}
