package goorm.athena.global.jwt.util;

public class LoginMapper {
    public static LoginInfoDto toLoginInfo(Long userId, String email){
        return new LoginInfoDto(
                userId,
                email
        );
    }
}
