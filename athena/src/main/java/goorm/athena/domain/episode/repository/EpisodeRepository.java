package goorm.athena.domain.episode.repository;

import goorm.athena.domain.episode.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    // List<Episode> findByNovelId(Long novelId);

}
