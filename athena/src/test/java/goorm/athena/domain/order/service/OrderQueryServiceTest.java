package goorm.athena.domain.order.service;
import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.order.OrderIntegrationTestSupport;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import static goorm.athena.util.TestEntityFactory.createUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static goorm.athena.util.TestEntityFactory.*;
import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

public class OrderQueryServiceTest extends OrderIntegrationTestSupport {

    @Test
    @DisplayName("getById - 주문 ID로 주문 정보를 조회할 수 있다")
    void getById_success() {
        // given
        ImageGroup imageGroup = imageGroupRepository.save(new ImageGroup());
        User buyer = userRepository.save(createUser("buyer@example.com", "test1234!", "nickname", imageGroup, Role.ROLE_USER));

        Category category = categoryRepository.save(createCategory("카테고리"));
        BankAccount bankAccount = bankAccountRepository.save(
                createBankAccount(buyer, "123-456", "홍길동", "신한은행", true)
        );
        PlatformPlan plan = platformPlanRepository.findById(1L).orElseThrow();
        Project project = projectRepository.save(
                setupFullProject(buyer, category, imageGroup, bankAccount, plan)
        );

        Order order = orderRepository.save(Order.create(buyer, null, project, LocalDateTime.now()));

        // when
        Order found = orderQueryService.getById(order.getId());

        // then
        assertThat(found.getId()).isEqualTo(order.getId());
    }
    @Test
    @DisplayName("getById - 존재하지 않는 주문 ID 조회시 예외 발생")
    void getById_notFound() {
        Long invalidId = 9999L;

        assertThatThrownBy(() -> orderQueryService.getById(invalidId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.ORDER_NOT_FOUND.getErrorMessage());
    }


    @DisplayName("Order ID로 판매자 ID를 조회한다")
    @Test
    void getSeller_success() {
        // given
        ImageGroup imageGroup = imageGroupRepository.save(new ImageGroup());
        User seller = userRepository.save(createUser("seller@test.com", "1234", "판매자", imageGroup, Role.ROLE_USER));
        User buyer = userRepository.save(createUser("buyer@test.com", "1234", "구매자", imageGroup, Role.ROLE_USER));

        Category category = categoryRepository.save(createCategory("카테고리"));
        BankAccount bankAccount = bankAccountRepository.save(createBankAccount(seller, "123-456", "홍길동", "신한은행", true));
        PlatformPlan platformPlan = platformPlanRepository.findById(1L).orElseThrow();
        Project project = projectRepository.save(setupFullProject(seller, category, imageGroup, bankAccount, platformPlan));
        Product product = productRepository.save(createProduct(project, "상품", "설명", 1000L, 10L));

        Order order = orderRepository.save(Order.create(buyer, null, project, null));

        // when
        Long sellerId = orderQueryService.getSeller(order.getId());

        // then
        assertThat(sellerId).isEqualTo(seller.getId());
    }

    @DisplayName("Order ID로 구매자 ID를 조회한다")
    @Test
    void getBuyer_success() {
        // given
        ImageGroup imageGroup = imageGroupRepository.save(new ImageGroup());
        User seller = userRepository.save(createUser("seller@test.com", "1234", "판매자", imageGroup, Role.ROLE_USER));
        User buyer = userRepository.save(createUser("buyer@test.com", "1234", "구매자", imageGroup, Role.ROLE_USER));

        Category category = categoryRepository.save(createCategory("카테고리"));
        BankAccount bankAccount = bankAccountRepository.save(createBankAccount(seller, "123-456", "홍길동", "신한은행", true));
        PlatformPlan platformPlan = platformPlanRepository.findById(1L).orElseThrow();
        Project project = projectRepository.save(setupFullProject(seller, category, imageGroup, bankAccount, platformPlan));
        Product product = productRepository.save(createProduct(project, "상품", "설명", 1000L, 10L));

        Order order = orderRepository.save(Order.create(buyer, null, project, null));

        // when
        Long buyerId = orderQueryService.getBuyer(order.getId());

        // then
        assertThat(buyerId).isEqualTo(buyer.getId());
    }

}
