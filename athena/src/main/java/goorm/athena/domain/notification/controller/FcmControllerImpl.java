package goorm.athena.domain.notification.controller;


import goorm.athena.domain.notification.dto.FcmLoginRequest;
import goorm.athena.domain.notification.service.MessageFactory;
import goorm.athena.domain.notification.service.NotificationService;
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
    private final NotificationService notificationService;
    private final UserQueryService userQueryService;
    private final MessageFactory messageFactory;

    @Override
    public ResponseEntity<Void> createToken(@RequestBody FcmLoginRequest fcmLoginRequest){
        fcmTokenService.saveToken(fcmLoginRequest);
        User user = userQueryService.getUser(fcmLoginRequest.userId());
        notificationService.notifyLogin(fcmLoginRequest.userId(), user.getNickname());

        return ResponseEntity.ok().build();
    }

    /***
     * 알림 테스트용 API
     */
    @Override
    public ResponseEntity<Void> testToUser(@RequestParam("userId") Long userId){
        notificationService.notifyReview(userId, "테스트 프로젝트");
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> testToAll(){
        notificationService.notifyCoupon("테스트 쿠폰");
        return ResponseEntity.ok().build();
    }


}
