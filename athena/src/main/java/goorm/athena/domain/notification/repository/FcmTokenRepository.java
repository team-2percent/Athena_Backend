package goorm.athena.domain.notification.repository;

import goorm.athena.domain.notification.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByUserId(Long userId);

    @Query(value = "SELECT * FROM fcm_token", nativeQuery = true)
    List<FcmToken> findAllTokens();
}

