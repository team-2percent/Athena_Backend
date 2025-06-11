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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class MyInfoServiceTest extends MyInfoIntegrationTestSupport{

    private static final Logger log = LoggerFactory.getLogger(MyInfoServiceTest.class);

    @DisplayName("로그인 한 유저의  등록한 프로젝트들을 성공적으로 무한 페이징 형식으로 조회한다.")
    @Test
    void getMyProjects_Success() {
        // given
        User user = userRepository.findById(2L).get();

        MyProjectScrollRequest request = new MyProjectScrollRequest(
                LocalDateTime.now(), 10L, 5
        );

        Project project = projectService.getById(2L);

        // when
        MyProjectScrollResponse result = myInfoQueryService.getMyProjects(user.getId(), request);

        // then
        assertThat(result.content()).isNotEmpty();
        assertThat(result.content().getLast().title()).isEqualTo(project.getTitle());
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