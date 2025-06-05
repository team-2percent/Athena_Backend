package goorm.athena.domain.deliveryinfo.dto.req;

import jakarta.persistence.Column;

public record DeliveryInfoRequest(
     String zipcode,
     String address,

     @Column(length = 100)
     String detailAddress
) {}
