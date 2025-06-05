package goorm.athena.domain.deliveryinfo.repository;

import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryInfoRepository extends JpaRepository<DeliveryInfo, Long> {
    List<DeliveryInfo> findByUserId(Long userId);
    Optional<DeliveryInfo> findByUserIdAndIsDefaultTrue(Long userId);
    boolean existsByUserIdAndIsDefaultTrue(Long userId);
}