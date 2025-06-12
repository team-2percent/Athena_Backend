package goorm.athena.domain.user.service;

import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.MyInfoIntegrationTestSupport;
import goorm.athena.domain.user.dto.response.MyOrderScrollRequest;
import goorm.athena.domain.user.dto.response.MyOrderScrollResponse;
import goorm.athena.domain.user.dto.response.MyProjectScrollRequest;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import goorm.athena.domain.user.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class MyInfoServiceTest extends MyInfoIntegrationTestSupport{


    @DisplayName("(data.sql 프로젝트의 2번, 21~30번 사용 중)" +
            "로그인한 사용자가 등록한 프로젝트 존재하고 거절된 프로젝트가(30, 27, 24, 21) 존재할 때 첫 페이지(29, 28, 26) 요청 시" +
            "로그인 한 유저의 등록한 프로젝트들을 무한 스크롤 형식으로 성공적으로 조회한다.")
    @Test
    void getMyProjects_Success_FirstPage() {
        // given
        User user = userRepository.findById(2L).get();

        MyProjectScrollRequest request = new MyProjectScrollRequest(
                null,
                null,
                3
        );

        Project project = projectRepository.findById(29L).get();
        Project project2 = projectRepository.findById(28L).get();
        Project project3 = projectRepository.findById(26L).get();

        // when
        MyProjectScrollResponse response = myInfoQueryService.getMyProjects(user.getId(), request);

        // then
        assertThat(response.content().size()).isEqualTo(3);
        assertThat(response.content().get(0).title()).isEqualTo(project.getTitle());
        assertThat(response.content().get(0).projectId()).isEqualTo(project.getId());

        assertThat(response.content().get(1).title()).isEqualTo(project2.getTitle());
        assertThat(response.content().get(1).projectId()).isEqualTo(project2.getId());

        assertThat(response.content().get(2).title()).isEqualTo(project3.getTitle());
        assertThat(response.content().get(2).projectId()).isEqualTo(project3.getId());

        // 다음 페이지를 조회하기 위한 커서 기준 값 검증
        assertThat(response.nextProjectId()).isEqualTo(project3.getId());
        assertThat(response.nextCursorValue()).isEqualTo(project3.getCreatedAt());
    }

    @DisplayName("(data.sql 프로젝트의 2번, 21~30번 사용 중)" +
            "로그인한 사용자가 등록한 프로젝트 존재하고 거절된 프로젝트가(30, 27, 24, 21) 존재할 때 두 번째 페이지(25, 23, 22) 요청 시" +
            "로그인 한 유저의 등록한 프로젝트들을 무한 스크롤 형식으로 성공적으로 조회한다.")
    @Test
    void getMyProjects_Success_SecondPage() {
        // given
        User user = userRepository.findById(2L).get();

        MyProjectScrollRequest request = new MyProjectScrollRequest(
                LocalDateTime.parse("2025-01-26T00:00"),
                26L,
                3
        );

        Project project = projectRepository.findById(25L).get();
        Project project2 = projectRepository.findById(23L).get();
        Project project3 = projectRepository.findById(22L).get();

        // when
        MyProjectScrollResponse response = myInfoQueryService.getMyProjects(user.getId(), request);

        // then
        assertThat(response.content().size()).isEqualTo(3);
        assertThat(response.content().get(0).title()).isEqualTo(project.getTitle());
        assertThat(response.content().get(0).projectId()).isEqualTo(project.getId());

        assertThat(response.content().get(1).title()).isEqualTo(project2.getTitle());
        assertThat(response.content().get(1).projectId()).isEqualTo(project2.getId());

        assertThat(response.content().get(2).title()).isEqualTo(project3.getTitle());
        assertThat(response.content().get(2).projectId()).isEqualTo(project3.getId());


        System.out.println(response.nextProjectId()+"123123");
        System.out.println(response.nextCursorValue()+"123123");
        // 다음 페이지를 조회하기 위한 커서 기준 값 검증
        assertThat(response.nextProjectId()).isEqualTo(project3.getId());
        assertThat(response.nextCursorValue()).isEqualTo(project3.getCreatedAt());
    }

    @DisplayName("(data.sql 프로젝트의 2번, 21~30번 사용 중)" +
            "로그인한 사용자가 등록한 프로젝트 존재하고 거절된 프로젝트가(30, 27, 24, 21) 존재할 때 세 번째 페이지(2, 30, 27) 요청 시" +
            "성공한 프로젝트 조회를 끝낸 후, 거절 프로젝트를 이어서 조회한다" +
            "로그인 한 유저의 등록한 프로젝트들을 무한 스크롤 형식으로 성공적으로 조회한다.")
    @Test
    void getMyProjects_Success_ThirdPage() {
        // given
        User user = userRepository.findById(2L).get();

        MyProjectScrollRequest request = new MyProjectScrollRequest(
                LocalDateTime.parse("2025-01-22T00:00"),
                22L,
                3
        );

        Project project = projectRepository.findById(2L).get();
        Project project2 = projectRepository.findById(30L).get();
        Project project3 = projectRepository.findById(27L).get();

        // when
        MyProjectScrollResponse response = myInfoQueryService.getMyProjects(user.getId(), request);

        System.out.println(response.nextCursorValue() + "123123");
        System.out.println(response.nextProjectId() + "123123");

        // then
        assertThat(response.content().size()).isEqualTo(3);
        assertThat(response.content().get(0).title()).isEqualTo(project.getTitle());
        assertThat(response.content().get(0).projectId()).isEqualTo(project.getId());

        assertThat(response.content().get(1).title()).isEqualTo(project2.getTitle());
        assertThat(response.content().get(1).projectId()).isEqualTo(project2.getId());

        assertThat(response.content().get(2).title()).isEqualTo(project3.getTitle());
        assertThat(response.content().get(2).projectId()).isEqualTo(project3.getId());

    }

    @DisplayName("(data.sql 프로젝트의 2번, 21~30번 사용 중)" +
            "로그인한 사용자가 등록한 프로젝트 존재하고 거절된 프로젝트가(30, 27, 24, 21) 존재할 때 네 번째 페이지(24, 21, null) 요청 시" +
            "성공한 프로젝트 조회를 끝낸 후, 거절 프로젝트를 이어서 조회한다" +
            "거절된 프로젝트 목록을 이어서 무한 스크롤 형식으로 성공적으로 조회하고, 조회 못한 데이터는 제외한다.")
    @Test
    void getMyProjects_Success_FourthPage() {
        // given
        User user = userRepository.findById(2L).get();

        MyProjectScrollRequest request = new MyProjectScrollRequest(
                LocalDateTime.parse("2025-01-27T00:00"),
                27L,
                3
        );

        Project project = projectRepository.findById(24L).get();
        Project project2 = projectRepository.findById(21L).get();

        // when
        MyProjectScrollResponse response = myInfoQueryService.getMyProjects(user.getId(), request);

        // then
        assertThat(response.content().size()).isEqualTo(2);
        assertThat(response.content().get(0).title()).isEqualTo(project.getTitle());
        assertThat(response.content().get(0).projectId()).isEqualTo(project.getId());

        assertThat(response.content().get(1).title()).isEqualTo(project2.getTitle());
        assertThat(response.content().get(1).projectId()).isEqualTo(project2.getId());

        assertThat(response.nextCursorValue()).isNull();
        assertThat(response.nextProjectId()).isNull();
    }

    @Transactional
    @DisplayName("로그인 한 유저의 내가 구매한 상품들을 성공적으로 무한 페이징 형식으로 조회한다.")
    @Test
    void getMyOrders_Success() {
        // given
        User user = userRepository.findById(2L).get();

        DeliveryInfo deliveryInfo = deliveryInfoRepository.findByUserId(user.getId()).get(0);

        Project project = projectService.getById(2L);
        List<Product> products = productRepository.findAllByProject(project);

        Order order = setupOrder(user, deliveryInfo, project, LocalDateTime.now().minusDays(1));
        Order order2 = setupOrder(user, deliveryInfo, project, LocalDateTime.now().minusDays(2));

        OrderItem orderItem1 = setupOrderItem(order, products.get(0), 123, 12L);
        OrderItem orderItem2 = setupOrderItem(order2, products.get(1), 123, 123L);
        OrderItem orderItem3 = setupOrderItem(order, products.get(2), 123, 123L);

        orderRepository.saveAll(List.of(order, order2));
        orderItemRepository.saveAll(List.of(orderItem1, orderItem2, orderItem3));

        LocalDateTime cursor = LocalDateTime.now().minusDays(1);
        Long nextOrderId = 2L;
        int pageSize = 3;
        MyOrderScrollRequest request = new MyOrderScrollRequest(
                cursor,
                nextOrderId,
                pageSize
        );

        // when
        MyOrderScrollResponse result = myInfoQueryService.getMyOrders(user.getId(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(3);
        MyOrderScrollResponse.Item firstItem = result.content().get(0);
        assertThat(firstItem.projectName()).isEqualTo(project.getTitle());
        assertThat(firstItem.sellerName()).isEqualTo(user.getNickname());
    }
}