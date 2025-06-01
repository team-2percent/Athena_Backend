package goorm.athena.domain.user.service;

import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.MyInfoIntegrationTestSupport;
import goorm.athena.domain.user.UserIntegrationTestSupport;
import goorm.athena.domain.user.dto.response.MyOrderScrollRequest;
import goorm.athena.domain.user.dto.response.MyOrderScrollResponse;
import goorm.athena.domain.user.dto.response.MyProjectScrollRequest;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class MyInfoServiceTest extends MyInfoIntegrationTestSupport{

    @DisplayName("로그인 한 유저의 내가 등록한 프로젝트들을 성공적으로 무한 페이징 형식으로 조회한다.")
    @Test
    void getMyProjects_Success() {
        // given
        Long userId = 1L;
        MyProjectScrollRequest request = new MyProjectScrollRequest(
                LocalDateTime.now(), 10L, 5
        );

        MyProjectScrollResponse rawResponse = new MyProjectScrollResponse(
                List.of(new MyProjectScrollResponse.ProjectPreview(
                        1L,
                        "테스트 프로젝트",
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now().plusDays(10),
                        80L,
                        "https://image.com/sample.jpg"
                )),
                LocalDateTime.now().plusDays(1),
                2L
        );

        MyProjectScrollResponse mappedResponse = new MyProjectScrollResponse(
                rawResponse.content(),
                rawResponse.nextCursorValue(),
                rawResponse.nextProjectId()
        );

        given(myInfoQueryRepository.findMyProjectsByCursor(
                userId, request.nextCursorValue(), request.nextProjectId(), request.pageSize()))
                .willReturn(rawResponse);

        given(myProjectScrollResponseMapper.toResponse(
                rawResponse.content(), rawResponse.nextCursorValue(), rawResponse.nextProjectId()))
                .willReturn(mappedResponse);

        // when
        MyProjectScrollResponse result = myInfoService.getMyProjects(userId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).title()).isEqualTo("테스트 프로젝트");
        assertThat(result.nextProjectId()).isEqualTo(2L);
    }

    @DisplayName("로그인 한 유저의 내가 구매한 상품들을 성공적으로 무한 페이징 형식으로 조회한다.")
    @Test
    void getMyOrders_Success() {
        // given
        Long userId = 1L;

        LocalDateTime cursor = LocalDateTime.now().minusDays(1);
        Long nextOrderId = 3L;
        int pageSize = 3;
        MyOrderScrollRequest request = new MyOrderScrollRequest(cursor, nextOrderId, pageSize);

        MyOrderScrollResponse.Item item = new MyOrderScrollResponse.Item(
                1L, 1L, 1L, "프로젝트명", "상품명", "판매자명",
                "https://image.com/sample.jpg",
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(10),
                75L,
                false
        );

        MyOrderScrollResponse expectedResponse = new MyOrderScrollResponse(
                List.of(item),
                LocalDateTime.now().plusDays(1),
                2L
        );

        given(myInfoQueryRepository.findOrdersByUserIdWithScroll(
                eq(userId), any(MyOrderScrollRequest.class)))
                .willReturn(expectedResponse);

        // when
        MyOrderScrollResponse result = myInfoService.getMyOrders(userId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).projectName()).isEqualTo("프로젝트명");
        assertThat(result.nextOrderId()).isEqualTo(2L);
    }
}