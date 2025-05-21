package goorm.athena.domain.deliveryinfo.dto.req;

public record DeliveryInfoUpdateRequest(
        String zipcode,
        String address,
        String detailAddress
) {}