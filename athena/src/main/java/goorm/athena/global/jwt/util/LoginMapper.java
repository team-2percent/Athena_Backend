package goorm.athena.global.jwt.util;

public class LoginMapper {
    public static LoginInfoDto toLoginInfo(Long userId, String nickname, String role){
        return new LoginInfoDto(
                userId,
                nickname,
                role
        );
    }

    public static LoginUserRequest toRequest(String nickname, Long userId, String role){
        return new LoginUserRequest(
                nickname,
                userId,
                role
        );
    }
}
