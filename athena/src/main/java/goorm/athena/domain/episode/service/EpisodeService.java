package goorm.athena.domain.episode.service;

import goorm.athena.domain.episode.dto.request.EpisodeAddRequest;
import goorm.athena.domain.episode.dto.response.EpisodeGetResponse;
import goorm.athena.domain.episode.entity.Episode;
import goorm.athena.domain.episode.mapper.EpisodeMapper;
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

    @Transactional
    public Episode addEpisode(Long novelId, EpisodeAddRequest request) {
        // 해당 novelId에 속한 모든 회차를 조회하여 마지막 회차 번호를 구함
        int lastEpisodeNumber = episodeRepository.findMaxEpisodeNumberByNovelId(novelId).orElse(0);

        Episode newEpisode = Episode.create(
                request.title(),
                request.content(),
                request.price(),
                lastEpisodeNumber+1
        );

        return episodeRepository.save(newEpisode);
    }

    // 해당 회차 전체 내용 조회
    @Transactional(readOnly = true)
    public Episode getEpisodeById(Long id){
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회차를 찾을 수 없습니다."));
        return episode;
    }

    // 회차 내용 페이지 형식으로 조회
    @Transactional(readOnly = true)
    public String getEpisodeContentById(Long id, int page, int pageSize) {
        Episode episode = episodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("회차를 찾을 수 없습니다."));

        String content = episode.getContent();

        // 페이지 단위로 잘라서 반환: page는 0부터 시작
        int startIdx = page * pageSize;
        int endIdx = Math.min(startIdx + pageSize, content.length());

        if (startIdx < content.length()) {
            return content.substring(startIdx, endIdx);
        } else {
            throw new RuntimeException("페이지 번호가 잘못되었습니다.");
        }
    }

    // 작품 회차 리스트 조회
    @Transactional
    public Page<Episode> getEpisodeByNovelId(Long novelId, Pageable pageable){
        return episodeRepository.findByNovelId(novelId, pageable);
    }

    @Transactional
    public void deleteEpisode(Long id){
        episodeRepository.deleteById(id);
    }

}
