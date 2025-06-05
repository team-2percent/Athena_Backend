package goorm.athena.domain.admin.service;


import goorm.athena.domain.user.entity.Role;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import goorm.athena.global.jwt.util.LoginUserRequest;
import org.springframework.stereotype.Component;

@Component
public class AdminRoleCheckService {
    public void checkAdmin(LoginUserRequest user) {
        if (user.role() != Role.ROLE_ADMIN) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }
}