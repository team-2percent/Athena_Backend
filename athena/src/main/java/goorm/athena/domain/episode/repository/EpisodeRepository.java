package goorm.athena.domain.episode.repository;

import goorm.athena.domain.episode.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    @Query("SELECT MAX(e.episodeId) FROM Episode e WHERE e.novel.id = :novelId")
    Optional<Integer> findMaxEpisodeNumberByNovelId(Long novelId);

    // Page<Episode> findByNovelId(Long novelId, Pageable pageable);

}
