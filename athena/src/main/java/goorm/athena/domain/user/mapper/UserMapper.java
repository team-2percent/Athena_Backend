package goorm.athena.domain.user.mapper;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.User;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "imageGroup", source = "imageGroup")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "password", source = "request.password")
    @Mapping(target = "nickname", source = "request.nickname")
    User toEntity(UserCreateRequest request, ImageGroup imageGroup);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "nickname", source = "user.nickname")
    UserCreateResponse toCreateResponse(User user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "sellerIntroduction", source = "user.sellerIntroduction")
    @Mapping(target = "linkUrl", source = "user.linkUrl")
    UserDetailResponse toDetailResponse(User user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "sellerIntroduction", source = "user.sellerIntroduction")
    @Mapping(target = "linkUrl", source = "user.linkUrl")
    UserUpdateResponse toUpdateResponse(User user);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "sellerDescription", source = "user.sellerIntroduction")
    @Mapping(target = "imageUrl", expression = "java(imageUrl)")
    UserGetResponse toGetResponse(User user, @Context String imageUrl);

    UserLoginResponse toLoginResponse(Long userId, String accessToken, String refreshToken);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "nickname", source = "user.nickname")
    @Mapping(target = "imageUrl", expression = "java(imageUrl)")
    UserHeaderGetResponse toHeaderGetResponse(User user, @Context String imageUrl);

    UserSummaryResponse toSummaryResponse(User user);
}