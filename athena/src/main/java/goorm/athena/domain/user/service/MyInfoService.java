package goorm.athena.domain.user.service;

import goorm.athena.domain.user.dto.response.MyOrderScrollRequest;
import goorm.athena.domain.user.dto.response.MyOrderScrollResponse;
import goorm.athena.domain.user.dto.response.MyProjectScrollRequest;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import goorm.athena.domain.user.mapper.MyProjectScrollResponseMapper;
import goorm.athena.domain.user.repository.MyInfoQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyInfoService {

    private final MyInfoQueryRepository myInfoQueryRepository;
    private final MyProjectScrollResponseMapper myProjectScrollResponseMapper;

    public MyProjectScrollResponse getMyProjects(Long userId, MyProjectScrollRequest request) {
        MyProjectScrollResponse rawResponse = myInfoQueryRepository.findMyProjectsByCursor(
                userId,
                request.nextCursorValue(),
                request.nextProjectId(),
                request.pageSize()
        );

        // 이미지 URL 가공 및 최종 응답 생성
        return myProjectScrollResponseMapper.toResponse(
                rawResponse.content(),
                rawResponse.nextCursorValue(),
                rawResponse.nextProjectId()
        );
    }

    public MyOrderScrollResponse getMyOrders(Long userId, MyOrderScrollRequest request) {
        return myInfoQueryRepository.findOrdersByUserIdWithScroll(userId, request);
    }
}
