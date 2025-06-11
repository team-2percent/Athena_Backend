package goorm.athena.domain.order.domain;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.order.OrderIntergrationTestSupport;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.order.entity.Status;
import goorm.athena.domain.user.entity.Role;
import goorm.athena.util.TestEntityFactory;
import org.junit.jupiter.api.DisplayName;

import goorm.athena.domain.deliveryinfo.entity.DeliveryInfo;

import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

public class OrderEntityTest  extends OrderIntergrationTestSupport {

    @Test
    @DisplayName("User, Project, DeliveryInfo 엔티티를 활용해 주문 생성 시, " +
            "주문 상태는 ORDERED이고 isSettled(정산여부)는 false로 초기화되어야 한다")
    void createOrder_success() {
        // given
        ImageGroup imageGroup = new ImageGroup();
        User user = TestEntityFactory.createUser("test@example.com", "password", "nickname", imageGroup, Role.ROLE_USER);
        DeliveryInfo delivery = TestEntityFactory.createDeliveryInfo(user, "12345", "서울시 강남구", "101호", false);
        Project project = TestEntityFactory.createProject(user, null, imageGroup, null, null,
                "제목", "설명", 100000L, 0L, "마크다운");
        LocalDateTime now = LocalDateTime.now();

        // when
        Order order = Order.create(user, delivery, project, now);

        // then
        assertThat(order.getUser()).isEqualTo(user);
        assertThat(order.getProject()).isEqualTo(project);
        assertThat(order.getDelivery()).isEqualTo(delivery);
        assertThat(order.getOrderedAt()).isEqualTo(now);
        assertThat(order.getStatus()).isEqualTo(Status.ORDERED);
        assertThat(order.isSettled()).isFalse();
    }

    @Test
    @DisplayName("completeOrder()를 호출하면 지정한 수량(3)과 금액(30000)이 주문의 필드(총가격,수량) 값이 적용된다")
    void completeOrder_success() {
        // given
        ImageGroup imageGroup = new ImageGroup();
        User user = TestEntityFactory.createUser("test@example.com", "password", "nickname", imageGroup, Role.ROLE_USER);
        DeliveryInfo delivery = TestEntityFactory.createDeliveryInfo(user, "12345", "서울시 강남구", "101호", false);
        Project project = TestEntityFactory.createProject(user, null, imageGroup, null, null,
                "제목", "설명", 100000L, 0L, "마크다운");

        Order order = TestEntityFactory.createOrder(user, delivery, project, LocalDateTime.now());

        // when
        order.completeOrder(30000L, 3);

        // then
        assertThat(order.getQuantity()).isEqualTo(3);
        assertThat(order.getTotalPrice()).isEqualTo(30000L);
    }

    @Test
    @DisplayName("주문을 생성하면 isSettled(정산여부) 초기값은 false로 생성되고" +
            " markAsSettled() 호출 시 정산 상태가 true로 변경된다")
    void markAsSettled_success() {
        // given
        ImageGroup imageGroup = new ImageGroup();
        User user = TestEntityFactory.createUser("test@example.com", "password", "nickname", imageGroup, Role.ROLE_USER);
        DeliveryInfo delivery = TestEntityFactory.createDeliveryInfo(user, "12345", "서울시 강남구", "101호", false);

        Project project = TestEntityFactory.createProject(user, null, imageGroup, null, null,
                "제목", "설명", 100000L, 0L, "마크다운");
        Order order = TestEntityFactory.createOrder(user, delivery, project, LocalDateTime.now());

        // then - 초기 상태는 false여야 한다
        assertThat(order.isSettled()).isFalse();
        // when
        order.markAsSettled();

        // then
        assertThat(order.isSettled()).isTrue();
    }
}