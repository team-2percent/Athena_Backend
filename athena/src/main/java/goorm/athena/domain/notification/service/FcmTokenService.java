package goorm.athena.domain.notification.service;

import goorm.athena.domain.notification.dto.FcmLoginRequest;
import goorm.athena.domain.notification.entity.FcmToken;
import goorm.athena.domain.notification.repository.FcmTokenRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    // 토큰 저장 (사용자마다 유니크한 FCM 토큰 발급)
    public void saveToken(FcmLoginRequest fcmLoginRequest) {
        String token = fcmLoginRequest.token();
        Long userId = fcmLoginRequest.userId();

        fcmTokenRepository.deleteByToken(token);
        fcmTokenRepository.deleteByUserId(userId);

        fcmTokenRepository.save(new FcmToken(token, userId));
    }

    // 사용자 ID 기반으로 FCM 토큰을 삭제
    public void deleteToken(Long userId) {
        fcmTokenRepository.findByUserId(userId).ifPresentOrElse(
                fcmTokenRepository::delete,
                () -> { throw new CustomException(ErrorCode.FAILED_TO_DELETE_FCM); }
        );
    }

    public String getToken(Long userId) {
        return fcmTokenRepository.findByUserId(userId)
                .map(FcmToken::getToken)
                .orElse(null); // FCM 토큰이 없을 경우 null 반환
    }

    public List<FcmToken> getAllToken(){
        return fcmTokenRepository.findAllTokens();
    }

}
