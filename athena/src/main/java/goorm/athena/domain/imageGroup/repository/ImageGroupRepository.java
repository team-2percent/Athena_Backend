package goorm.athena.domain.imageGroup.repository;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageGroupRepository extends JpaRepository<ImageGroup, Long> {
}
