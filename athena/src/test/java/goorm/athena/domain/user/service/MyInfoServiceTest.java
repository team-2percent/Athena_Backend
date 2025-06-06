package goorm.athena.domain.user.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.orderitem.entity.OrderItem;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
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

class MyInfoServiceTest extends MyInfoIntegrationTestSupport{

    @DisplayName("로그인 한 유저의 내가 등록한 프로젝트들을 성공적으로 무한 페이징 형식으로 조회한다.")
    @Test
    void getMyProjects_Success() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("test2@email.com", "1231231", "nickname2", imageGroup);
        Category category = setupCategory("음식");
        BankAccount bankAccount = setupBankAccount(user, "123" ,"123" ,"123", true);
        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();
        Project project = setupProject(user, category, imageGroup, bankAccount, platformPlan,
                "프로젝2132132131트 제목", "설123213213명", 100000L, 10000L, "!23");

        userRepository.save(user);
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);
        projectRepository.save(project);

        MyProjectScrollRequest request = new MyProjectScrollRequest(
                LocalDateTime.now(), 10L, 5
        );

        // when
        MyProjectScrollResponse result = myInfoService.getMyProjects(user.getId(), request);

        // then
        assertThat(result.content()).isNotEmpty();
        assertThat(result.content().get(0).title()).isEqualTo("프로젝2132132131트 제목");
    }

    @Transactional
    @DisplayName("로그인 한 유저의 내가 구매한 상품들을 성공적으로 무한 페이징 형식으로 조회한다.")
    @Test
    void getMyOrders_Success() {
        // given
        ImageGroup imageGroup = setupImageGroup();
        User user = setupUser("test@email.com", "123123", "nickname", imageGroup);
        Category category = setupCategory("프로그래밍");
        BankAccount bankAccount = setupBankAccount(user, "1243" ,"1243" ,"1243", true);
        PlatformPlan platformPlan = platformPlanRepository.findById(1L).get();
        Project project = setupProject(user, category, imageGroup, bankAccount, platformPlan,
                "프로젝213213211트 제목", "설12313213명", 1000000L, 100000L, "!23");
        DeliveryInfo deliveryInfo = setupDeliveryInfo(user, "12123123", "123123", "123213", true);
        Product product = setupProduct(project, "123", "123", 12L, 12L);
        Order order = setupOrder(user, deliveryInfo, project, LocalDateTime.now().minusDays(1));
        Order order2 = setupOrder(user, deliveryInfo, project, LocalDateTime.now().minusDays(2));

        OrderItem orderItem1 = setupOrderItem(order, product, 123, 12L);
        OrderItem orderItem2 = setupOrderItem(order2, product, 123, 123L);
        OrderItem orderItem3 = setupOrderItem(order2, product, 123, 123L);
        OrderItem orderItem4 = setupOrderItem(order2, product, 123, 123L);

        userRepository.save(user);
        categoryRepository.save(category);
        bankAccountRepository.save(bankAccount);
        projectRepository.save(project);
        deliveryInfoRepository.save(deliveryInfo);
        productRepository.save(product);
        orderRepository.saveAll(List.of(order, order2));
        orderItemRepository.saveAll(List.of(orderItem1, orderItem2, orderItem3, orderItem4));

        LocalDateTime cursor = LocalDateTime.now().minusDays(1);
        Long nextOrderId = 2L;
        int pageSize = 3;
        MyOrderScrollRequest request = new MyOrderScrollRequest(
                cursor,
                nextOrderId,
                pageSize
        );

        // when
        MyOrderScrollResponse result = myInfoService.getMyOrders(user.getId(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(3);
        MyOrderScrollResponse.Item firstItem = result.content().get(0);
        assertThat(firstItem.projectName()).isEqualTo(project.getTitle());
        assertThat(firstItem.sellerName()).isEqualTo(user.getNickname());
        assertThat(firstItem.endAt()).isEqualTo(project.getEndAt());
    }
}