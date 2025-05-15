package goorm.athena.domain.user.service;

import goorm.athena.domain.user.dto.response.MyProjectScrollRequest;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import goorm.athena.domain.user.repository.MyInfoQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyInfoService {

    private final MyInfoQueryRepository myInfoQueryRepository;

    public MyProjectScrollResponse getMyProjects(Long userId, MyProjectScrollRequest request) {
        return myInfoQueryRepository.findMyProjectsByCursor(
                userId,
                request.nextCursorValue(),
                request.nextProjectId(),
                request.pageSize()
        );
    }
}
