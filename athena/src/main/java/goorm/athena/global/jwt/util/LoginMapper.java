package goorm.athena.global.jwt.util;

public class LoginMapper {
    public static LoginInfoDto toLoginInfo(Long userId, String email, String role){
        return new LoginInfoDto(
                userId,
                email,
                role
        );
    }

    public static LoginUserRequest toRequest(String email, Long userId, String role){
        return new LoginUserRequest(
                email,
                userId,
                role
        );
    }
}
