package goorm.athena.domain.deliveryinfo.mapper;

import goorm.athena.domain.deliveryinfo.dto.req.DeliveryInfoRequest;
import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeliveryInfoMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "zipcode", source = "request.zipcode")
    @Mapping(target = "address", source = "request.address")
    @Mapping(target = "detailAddress", source = "request.detailAddress")
    DeliveryInfo toEntity(User user, DeliveryInfoRequest request, boolean isDefault);

    List<DeliveryInfoResponse> toGetResponse(List<DeliveryInfo> deliveryInfo);
}
