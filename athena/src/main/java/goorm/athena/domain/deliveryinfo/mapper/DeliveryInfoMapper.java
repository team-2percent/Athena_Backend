package goorm.athena.domain.deliveryinfo.mapper;

import goorm.athena.domain.deliveryinfo.dto.res.DeliveryInfoResponse;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeliveryInfoMapper {

    @Mapping(target = "user", source = "user")
    DeliveryInfo toEntity(User user, String zipcode, String address, String detailAddress, boolean isDefault);

    List<DeliveryInfoResponse> toGetResponse(List<DeliveryInfo> deliveryInfo);
}
