package goorm.athena.domain.notification.service;

import goorm.athena.domain.notification.dto.FcmLoginRequest;
import goorm.athena.domain.notification.entity.FcmToken;
import goorm.athena.domain.notification.repository.FcmTokenRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    public void saveToken(FcmLoginRequest fcmLoginRequest){
        String token = fcmLoginRequest.token();
        Long userId = fcmLoginRequest.userId();

        fcmTokenRepository.findByUserId(fcmLoginRequest.userId())
                .ifPresentOrElse(existing -> existing.updateToken(token),
                () -> fcmTokenRepository.save(new FcmToken(token, userId))
                );
    }

    public void deleteToken(Long userId){
        FcmToken token = fcmTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FAILED_TO_DELETE_FCM));

        fcmTokenRepository.delete(token);
    }

    public String getToken(Long userId){
        return fcmTokenRepository.findByUserId(userId)
                .map(FcmToken::getToken)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public List<FcmToken> getAllToken(){
        return fcmTokenRepository.findAllTokens();
    }

}
