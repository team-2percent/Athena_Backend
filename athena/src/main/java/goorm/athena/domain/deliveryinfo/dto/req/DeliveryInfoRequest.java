package goorm.athena.domain.deliveryinfo.dto.req;

public record DeliveryInfoRequest(
     String zipcode,
     String address,
     String detailAddress,
     boolean isDefault
) {}
