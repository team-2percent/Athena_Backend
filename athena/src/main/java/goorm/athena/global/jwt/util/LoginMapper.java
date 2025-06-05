package goorm.athena.global.jwt.util;

import goorm.athena.domain.user.entity.Role;

public class LoginMapper {
    public static LoginInfoDto toLoginInfo(Long userId, String nickname, Role role){
        return new LoginInfoDto(
                userId,
                nickname,
                role
        );
    }

    public static LoginUserRequest toRequest(String nickname, Long userId, Role role){
        return new LoginUserRequest(
                nickname,
                userId,
                role
        );
    }
}
