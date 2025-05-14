package goorm.athena.domain.settlementhistory.repository;

import goorm.athena.domain.orderitem.entity.QOrderItem;
import org.springframework.data.domain.Pageable;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.admin.dto.res.SettlementHistoryPageResponse;
import goorm.athena.domain.order.entity.QOrder;
import goorm.athena.domain.product.entity.QProduct;
import goorm.athena.domain.settlementhistory.entity.QSettlementHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SettlementHistoryQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<SettlementHistoryPageResponse.SettlementHistoryItem> findHistoriesBySettlementId(Long settlementId, Pageable pageable) {
        QSettlementHistory history = QSettlementHistory.settlementHistory;
        QOrder order = QOrder.order;
        QProduct product = QProduct.product;
        QOrderItem orderItem = QOrderItem.orderItem;

        List<SettlementHistoryPageResponse.SettlementHistoryItem> content = queryFactory
                .select(Projections.constructor(SettlementHistoryPageResponse.SettlementHistoryItem.class,
                        product.name,
                        orderItem.quantity,
                        history.totalPrice,
                        history.fee,
                        history.amount,
                        order.orderedAt
                ))
                .from(history)
                .join(history.order, order)
                .join(orderItem).on(orderItem.order.eq(order))
                .join(orderItem.product, product)
                .where(history.settlement.id.eq(settlementId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(order.orderedAt.desc())
                .fetch();

        Long total = queryFactory
                .select(history.count())
                .from(history)
                .where(history.settlement.id.eq(settlementId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}