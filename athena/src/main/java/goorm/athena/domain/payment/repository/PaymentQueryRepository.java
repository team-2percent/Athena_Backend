package goorm.athena.domain.payment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.order.entity.Order;
import goorm.athena.domain.payment.entity.QPayment;
import goorm.athena.domain.payment.entity.Status;
import goorm.athena.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Order> findUnsettledOrdersByProjects(List<Project> projects) {
        QPayment payment = QPayment.payment;

        return queryFactory
                .select(payment.order)
                .from(payment)
                .where(
                        payment.order.project.in(projects),
                        payment.order.isSettled.isFalse(),
                        payment.status.eq(Status.APPROVED),
                        payment.order.orderedAt.between(
                                payment.order.project.startAt,
                                payment.order.project.endAt
                        )
                )
                .fetch();
    }
}