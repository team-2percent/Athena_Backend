package goorm.athena.domain.user.mapper;

import goorm.athena.domain.user.dto.request.UserCreateRequest;
import goorm.athena.domain.user.dto.response.*;
import goorm.athena.domain.user.entity.User;

public class UserMapper {

    // UserCreateRequest -> User 엔티티 변환
    public static User toEntity(UserCreateRequest request){
        return User.builder()
                .email(request.email())
                .password(request.password())
                .nickname(request.nickname())
                .build();
    }

    // User -> UserCreateResponse Dto 변환
    public static UserCreateResponse toCreateResponse(User user){
        return new UserCreateResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole()
        );
    }

    public static UserDetailResponse toDetailResponse(User user){
        return new UserDetailResponse(
                user.getId(),
                user.getNickname(),
                user.getSellerIntroduction(),
                user.getLinkUrl()
        );
    }

    public static UserUpdateResponse toUpdateResponse(User user){
        return new UserUpdateResponse(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.getRole()
        );
    }

    public static UserGetResponse toGetResponse(User user){
        return new UserGetResponse(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getNickname(),
                user.getRole()
        );
    }

    public static UserLoginResponse toLoginResponse(Long userId, String accessToken, String refreshToken){
        return new UserLoginResponse(
                accessToken,
                refreshToken,
                userId
        );
    }
}
