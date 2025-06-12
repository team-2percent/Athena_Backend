package goorm.athena.domain.deliveryinfo.dto.res;

public record DeliveryInfoResponse(
        Long id,
        String zipcode,
        String address,
        String detailAddress,
        boolean isDefault
) { }