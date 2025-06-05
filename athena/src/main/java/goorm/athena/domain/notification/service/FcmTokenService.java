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

@Service
@RequiredArgsConstructor
public class FcmTokenService {

    private final FcmTokenRepository fcmTokenRepository;

    @Transactional
    // 토큰 저장 (사용자마다 유니크한 FCM 토큰 발급)
    public void saveToken(FcmLoginRequest fcmLoginRequest) {
        String token = fcmLoginRequest.token();
        Long userId = fcmLoginRequest.userId();

        // 기존 유저나 토큰은 조회 없이 바로 삭제
        fcmTokenRepository.deleteByToken(token);
        fcmTokenRepository.deleteByUserId(userId);

        // 토큰 저장
        fcmTokenRepository.save(new FcmToken(token, userId));
    }

    // 사용자 ID 기반으로 FCM 토큰을 삭제
    @Transactional
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
