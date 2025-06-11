package goorm.athena.domain.order.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.order.entity.QOrder;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final JPAQueryFactory queryFactory;


    public User findSellerByOrderId(Long orderId) {
        QOrder order = QOrder.order;
        QProject project = QProject.project;

        return queryFactory
                .select(project.seller)
                .from(order)
                .join(order.project, project)
                .where(order.id.eq(orderId))
                .fetchOne();
    }

    public User findBuyerByOrderId(Long orderId) {
        QOrder order = QOrder.order;
        return queryFactory
                .select(order.user)
                .from(order)
                .where(order.id.eq(orderId))
                .fetchOne();
    }
}
