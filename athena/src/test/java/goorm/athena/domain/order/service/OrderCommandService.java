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

//    @DisplayName("정상적인 주문 생성에 성공한다")
    @DisplayName("타입 USER 계정인 유저가 하나의 프로젝트에서 가격이 만원인 상품2개를 주문을 했다면, " +
            "주문테이블에서는 주문 수량(2)와 총가격(20000)의 데이터가 저장되어야 한다 ")
    @Test
    void createOrder_success() {
        // given
        // 유저 생성
        ImageGroup imageGroup = imageGroupRepository.save(new ImageGroup());
        User buyer = userRepository.save(createUser("buyer@example.com", "test1234!", "nickname", imageGroup, Role.ROLE_USER));
        DeliveryInfo delivery = deliveryInfoRepository.save(createDeliveryInfo(buyer, "12345", "서울시 강남구", "101호", true));
        BankAccount bankAccount = bankAccountRepository.save(createBankAccount(buyer, "123-456", "홍길동", "신한은행", true));

        // 프로젝트 생성
        Category category = categoryRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        PlatformPlan platformPlan = platformPlanRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECTPLAN_NOT_FOUND));
        Project project = projectRepository.save(
                setupFullProject(buyer, category, imageGroup, bankAccount, platformPlan)
        );
        Product product = productRepository.save(createProduct(project, "기본 상품", "기본 설명", 10_000L, 10L));

        // 상품 2개 주문 -> 주문 생성
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

    @DisplayName("상품 재고(10개)보다 많은 수량(15개)을 주문할 경우 '재고가 부족합니다' 예외 메시지와 함께 주문 생성에 실패해야 한다")
    @Test
    void createOrder_fail_dueToInsufficientStock() {
        // given
        ImageGroup imageGroup = imageGroupRepository.save(new ImageGroup());
        User buyer = userRepository.save(createUser("buyer@example.com", "test1234!", "nickname", imageGroup, Role.ROLE_USER));
        DeliveryInfo delivery = deliveryInfoRepository.save(createDeliveryInfo(buyer, "12345", "서울시 강남구", "101호", true));
        BankAccount bankAccount = bankAccountRepository.save(createBankAccount(buyer, "123-456", "홍길동", "신한은행", true));

        Category category = categoryRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        PlatformPlan platformPlan = platformPlanRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECTPLAN_NOT_FOUND));
        Project project = projectRepository.save(
                createProject(buyer, category, imageGroup, bankAccount, platformPlan,
                        "프로젝트 제목", "설명", 100000L, 0L, "마크다운")
        );
        Product product = productRepository.save(createProduct(project, "기본 상품", "기본 설명", 10000L, 10L));

        //재고 보다 많게 요청
        OrderItemRequest orderItem = new OrderItemRequest(product.getId(), 15);
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

    @DisplayName("결제 완료 후 상품 재고는 주문 수량만큼 차감되고, 프로젝트의 누적 후원 금액은 주문 금액만큼 증가해야 한다" +
            "(상품 가격이 1만 원이고 재고가 10개인 상태에서 3개를 주문하면, 재고는 7개가 되고 프로젝트 누적 금액은 3만 원으로 증가한다)")
    @Test
    void postPaymentProcess_success() {
        // given
        ImageGroup imageGroup = imageGroupRepository.save(new ImageGroup());
        User buyer = userRepository.save(createUser("buyer3@example.com", "test1234!", "nickname", imageGroup, Role.ROLE_USER));
        DeliveryInfo delivery = deliveryInfoRepository.save(createDeliveryInfo(buyer, "12345", "서울시 강남구", "101호", true));
        BankAccount bankAccount = bankAccountRepository.save(createBankAccount(buyer, "123-456", "홍길동", "신한은행", true));

        Category category = categoryRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        PlatformPlan platformPlan = platformPlanRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECTPLAN_NOT_FOUND));
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
        orderCommendService.postPaymentProcess(response.orderId()); // 상품 재고 감소 , 프로젝트 누적 금액 증가

        // then
        Product updatedProduct = productRepository.findById(product.getId())
                .orElseThrow(() ->new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        Project updatedProject = projectRepository.findById(project.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.PROJECT_NOT_FOUND));

        assertThat(updatedProduct.getStock()).isEqualTo(7);
        assertThat(updatedProject.getTotalAmount()).isEqualTo(30000);
    }
}
