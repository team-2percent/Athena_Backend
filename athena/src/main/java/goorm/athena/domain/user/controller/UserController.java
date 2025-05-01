package goorm.athena.domain.user.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "User", description = "유저 관련 API")
@RequestMapping("/api/user")
public interface UserController {
}
