package goorm.athena.domain.order.service;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.order.OrderIntergrationTestSupport;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.user.entity.Role;
import org.junit.jupiter.api.DisplayName;

import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;
import goorm.athena.domain.order.dto.req.OrderCreateRequest;
import goorm.athena.domain.order.dto.req.OrderItemRequest;
import goorm.athena.domain.order.dto.res.OrderCreateResponse;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static goorm.athena.util.TestEntityFactory.*;
import static org.assertj.core.api.Assertions.*;

public class OrderCommandService extends OrderIntergrationTestSupport {

    @DisplayName("정상적인 주문 생성에 성공한다")
    @Test
    void createOrder_success() {
        // given
        ImageGroup imageGroup = imageGroupRepository.save(new ImageGroup());
        User buyer = userRepository.save(createUser("buyer@example.com", "test1234!", "nickname", imageGroup, Role.ROLE_USER));

        DeliveryInfo delivery = deliveryInfoRepository.save(createDeliveryInfo(buyer, "12345", "서울시 강남구", "101호", true));

        Category category = categoryRepository.save(createCategory("카테고리"));
        BankAccount bankAccount = bankAccountRepository.save(
                createBankAccount(buyer, "123-456", "홍길동", "신한은행", true)
        );

        PlatformPlan platformPlan = platformPlanRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("PlatformPlan with id 1 not found"));

        Project project = projectRepository.save(
                setupFullProject(buyer, category, imageGroup, bankAccount, platformPlan)
        );

        Product product = productRepository.save(createProduct(project, "기본 상품", "기본 설명", 10_000L, 10L));

        OrderItemRequest orderItem = new OrderItemRequest(product.getId(), 2);
        OrderCreateRequest request = new OrderCreateRequest(
                project.getId(), delivery.getId(), List.of(orderItem)
        );

        // when
        OrderCreateResponse response = orderCommendService.createOrder(buyer.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.totalPrice()).isEqualTo(20_000L);
    }

    @DisplayName("상품 재고가 부족하면 주문 생성에 실패한다")
    @Test
    void createOrder_fail_dueToInsufficientStock() {
        // given
        ImageGroup imageGroup = imageGroupRepository.save(new ImageGroup());
        User buyer = userRepository.save(
                createUser("buyer@example.com", "test1234!", "nickname", imageGroup, Role.ROLE_USER)
        );

        DeliveryInfo delivery = deliveryInfoRepository.save(
                createDeliveryInfo(buyer, "12345", "서울시 강남구", "101호", true)
        );

        Category category = categoryRepository.save(createCategory("카테고리"));
        BankAccount bankAccount = bankAccountRepository.save(
                createBankAccount(buyer, "123-456", "홍길동", "신한은행", true)
        );

        PlatformPlan platformPlan = platformPlanRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("PlatformPlan with id 1 not found"));

        Project project = projectRepository.save(
                createProject(buyer, category, imageGroup, bankAccount, platformPlan,
                        "프로젝트 제목", "설명", 100000L, 0L, "마크다운")
        );

        Product product = productRepository.save(
                createProduct(project, "기본 상품", "기본 설명", 10000L, 10L)
        );

        OrderItemRequest orderItem = new OrderItemRequest(product.getId(), 15); // 재고보다 많은 요청
        OrderCreateRequest request = new OrderCreateRequest(
                project.getId(),
                delivery.getId(),
                List.of(orderItem)
        );

        // when & then
        assertThatThrownBy(() -> orderCommendService.createOrder(buyer.getId(), request))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(ErrorCode.INSUFFICIENT_INVENTORY.getErrorMessage());
    }

    @DisplayName("결제 후 상품 재고 차감 및 프로젝트 금액 증가")
    @Test
    void postPaymentProcess_success() {
        // given
        ImageGroup imageGroup = imageGroupRepository.save(new ImageGroup());

        User buyer = userRepository.save(createUser("buyer3@example.com", "test1234!", "nickname", imageGroup, Role.ROLE_USER));

        DeliveryInfo delivery = deliveryInfoRepository.save(createDeliveryInfo(buyer, "12345", "서울시 강남구", "101호", true));

        Category category = categoryRepository.save(createCategory("카테고리"));

        BankAccount bankAccount = bankAccountRepository.save(createBankAccount(buyer, "123-456", "홍길동", "신한은행", true));

        PlatformPlan platformPlan = platformPlanRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("PlatformPlan with id 1 not found"));

        Project project = projectRepository.save(createProject(buyer, category, imageGroup, bankAccount, platformPlan,
                        "프로젝트 제목", "설명", 100000L, 0L, "마크다운")
        );

        Product product = productRepository.save(createProduct(project, "기본 상품", "기본 설명", 10000L, 10L));

        OrderItemRequest orderItem = new OrderItemRequest(product.getId(), 3);
        OrderCreateRequest request = new OrderCreateRequest(
                project.getId(),
                delivery.getId(),
                List.of(orderItem)
        );

        OrderCreateResponse response = orderCommendService.createOrder(buyer.getId(), request);

        // when
        orderCommendService.postPaymentProcess(response.orderId());

        // then
        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Project updatedProject = projectRepository.findById(project.getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        assertThat(updatedProduct.getStock()).isEqualTo(7);
        assertThat(updatedProject.getTotalAmount()).isEqualTo(30000);
    }
}
