package goorm.athena.domain.notification.controller;


import goorm.athena.domain.notification.dto.FcmLoginRequest;
import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/fcm")
public class FcmControllerImpl implements FcmController{
    private final FcmTokenService fcmTokenService;
    private final FcmNotificationService fcmNotificationService;
    private final UserService userService;

    @Override
    public ResponseEntity<Void> createToken(@RequestBody FcmLoginRequest fcmLoginRequest){
        fcmTokenService.saveToken(fcmLoginRequest);
        User user = userService.getUser(fcmLoginRequest.userId());
        fcmNotificationService.notifyLogin(fcmLoginRequest.userId(), user.getNickname());

        return ResponseEntity.ok().build();
    }
}
