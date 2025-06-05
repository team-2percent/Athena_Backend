package goorm.athena.domain.deliveryinfo.dto.res;

import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;

public record DeliveryInfoResponse(
        Long id,
        String zipcode,
        String address,
        String detailAddress,
        boolean isDefault
) {
    public static DeliveryInfoResponse from(DeliveryInfo info) {
        return new DeliveryInfoResponse(
                info.getId(),
                info.getZipcode(),
                info.getAddress(),
                info.getDetailAddress(),
                info.isDefault()
        );
    }
}