package goorm.athena.domain.notification.controller;


import goorm.athena.domain.notification.dto.FcmLoginRequest;
import goorm.athena.domain.notification.service.FcmMessageFactory;
import goorm.athena.domain.notification.service.FcmNotificationService;
import goorm.athena.domain.notification.service.FcmTokenService;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/fcm")
public class FcmControllerImpl implements FcmController{
    private final FcmTokenService fcmTokenService;
    private final FcmNotificationService fcmNotificationService;
    private final UserQueryService userQueryService;
    private final FcmMessageFactory fcmMessageFactory;

    @Override
    public ResponseEntity<Void> createToken(@RequestBody FcmLoginRequest fcmLoginRequest){
        fcmTokenService.saveToken(fcmLoginRequest);
        User user = userQueryService.getUser(fcmLoginRequest.userId());
        fcmNotificationService.notifyLogin(fcmLoginRequest.userId(), user.getNickname());

        return ResponseEntity.ok().build();
    }

    /***
     * 알림 테스트용 API
     */
    @Override
    public ResponseEntity<Void> test(@RequestParam("token") String token){
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        fcmNotificationService.send(token, fcmMessageFactory.forReview("테스트 알림"));
        return ResponseEntity.ok().build();
    }

}
