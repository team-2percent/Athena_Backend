package goorm.athena.domain.image.repository;

import goorm.athena.domain.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("""
        SELECT i
        FROM Image i
        WHERE i.imageGroup.id = :imageGroupId
        AND i.isDefault = true
        """)
    Optional<Image> findFirstImageByImageGroupId(@Param("imageGroupId") Long imageGroupId);
}
