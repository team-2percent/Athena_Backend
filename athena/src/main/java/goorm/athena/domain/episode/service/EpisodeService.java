package goorm.athena.domain.episode.service;

import goorm.athena.domain.episode.entity.Episode;
import goorm.athena.domain.episode.repository.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class EpisodeService {

    private EpisodeRepository episodeRepository;

    @Transactional(readOnly = true)
    public Episode getEpisodeById(Long id){
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회차를 찾을 수 없습니다."));
        return episode;
    }
/*
    @Transactional
    public Page<Episode> getEpisodeByNovelId(Long novelId, Pageable pageable){
        return episodeRepository.findByNovelId(novelId, pageable);
    }

 */
    @Transactional
    public void deleteEpisode(Long id){
        episodeRepository.deleteById(id);
    }

}
