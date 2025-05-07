package goorm.athena.domain.user.repository;

import goorm.athena.domain.user.entity.RefreshToken;
import goorm.athena.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByValue(String value);

    Optional<RefreshToken> findByUser(User user);

    void deleteAllByUser(User user);
}
