package goorm.athena.domain.image.repository;

import goorm.athena.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
