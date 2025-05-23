package goorm.athena.domain.image.repository;

import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findAllByImageGroup(ImageGroup imageGroup);

    @Query("""
        SELECT i
        FROM Image i
        WHERE i.imageGroup.id = :imageGroupId
        AND i.imageIndex = 1
        """)
    Optional<Image> findFirstImageByImageGroupId(@Param("imageGroupId") Long imageGroupId);
}
