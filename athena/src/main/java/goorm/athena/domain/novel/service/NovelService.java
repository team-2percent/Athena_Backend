package goorm.athena.domain.novel.service;

import goorm.athena.domain.novel.dto.req.NovelCreateRequest;
import goorm.athena.domain.novel.dto.res.NovelCreateResponse;
import goorm.athena.domain.novel.entity.Novel;
import goorm.athena.domain.novel.repository.NovelRepository;
import goorm.athena.domain.user.entity.User;
import goorm.athena.domain.user.repository.UserRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NovelService {

    private final UserRepository userRepository;
    private final NovelRepository novelRepository;

    public NovelCreateResponse createNovel(NovelCreateRequest request) {

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOVEL_NOT_FOUND));

        Novel novel = Novel.create(user, request);

        Novel savedNovel = novelRepository.save(novel);

        return new NovelCreateResponse(
                savedNovel.getId()
        );
    }
}
