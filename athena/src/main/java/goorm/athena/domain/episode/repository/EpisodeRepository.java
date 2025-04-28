package goorm.athena.domain.episode.repository;

import goorm.athena.domain.episode.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    // Page<Episode> findByNovelId(Long novelId, Pageable pageable);

}
