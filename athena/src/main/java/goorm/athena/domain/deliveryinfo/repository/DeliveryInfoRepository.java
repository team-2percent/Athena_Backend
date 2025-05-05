package goorm.athena.domain.deliveryinfo.repository;

import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryInfoRepository extends JpaRepository<DeliveryInfo, Long> {
    List<DeliveryInfo> findByUserId(Long userId);

    // 기본 배송지 조회
    DeliveryInfo findByUserIdAndIsDefaultTrue(Long userId);
}