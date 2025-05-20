package goorm.athena.domain.settlement.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.admin.dto.res.ProductSettlementSummaryResponse;
import goorm.athena.domain.admin.dto.res.SettlementDetailInfoResponse;
import goorm.athena.domain.bankaccount.entity.QBankAccount;
import goorm.athena.domain.order.entity.QOrder;
import goorm.athena.domain.orderitem.entity.QOrderItem;
import goorm.athena.domain.product.entity.QProduct;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.settlement.dto.res.SettlementSummaryResponse;
import goorm.athena.domain.settlement.entity.QSettlement;
import goorm.athena.domain.settlement.entity.Status;
import goorm.athena.domain.settlementhistory.entity.QSettlementHistory;
import goorm.athena.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SettlementQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<SettlementSummaryResponse> findPageByFilters(Status status, Integer year, Integer month, Pageable pageable) {
        QSettlement settlement = QSettlement.settlement;
        QProject project = QProject.project;
        QUser user = QUser.user;

        BooleanBuilder builder = new BooleanBuilder();
        if (status != null) builder.and(settlement.status.eq(status));
        if (year != null) builder.and(settlement.requestedAt.year().eq(year));
        if (month != null) builder.and(settlement.requestedAt.month().eq(month));

        List<SettlementSummaryResponse> content = queryFactory
                .select(Projections.constructor(SettlementSummaryResponse.class,
                        settlement.id,
                        settlement.project.title,
                        settlement.totalSales,
                        settlement.platformFeeTotal,
                        settlement.payOutAmount,
                        settlement.user.nickname,
                        settlement.requestedAt,
                        settlement.status
                ))
                .from(settlement)
                .join(settlement.project, project)
                .join(settlement.user, user)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(
                        new CaseBuilder()
                                .when(settlement.status.eq(Status.PENDING)).then(0)
                                .otherwise(1).asc(),
                        settlement.requestedAt.desc()
                )
                .fetch();

        Long total = queryFactory
                .select(settlement.count())
                .from(settlement)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    public SettlementDetailInfoResponse findSettlementDetailInfo(Long settlementId) {
        QSettlement settlement = QSettlement.settlement;
        QProject project = QProject.project;
        QUser user = QUser.user;
        QBankAccount bankAccount = QBankAccount.bankAccount;

        return queryFactory
                .select(Projections.constructor(SettlementDetailInfoResponse.class,
                        project.title,
                        user.nickname,
                        user.id,
                        project.goalAmount,
                        settlement.totalSales,
                        settlement.payOutAmount,
                        settlement.platformFeeTotal,
                        settlement.totalCount,
                        settlement.settledAt,
                        settlement.status,
                        Projections.constructor(SettlementDetailInfoResponse.BankAccountInfo.class,
                                bankAccount.bankName,
                                bankAccount.accountNumber
                        ),
                        project.startAt,
                        project.endAt
                ))
                .from(settlement)
                .join(settlement.project, project)
                .join(settlement.user, user)
                .join(settlement.bankAccount, bankAccount)
                .where(settlement.id.eq(settlementId))
                .fetchOne();
    }


    public ProductSettlementSummaryResponse findProductSettlementsWithSummary(Long settlementId) {
        QSettlementHistory history = QSettlementHistory.settlementHistory;
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;

        List<ProductSettlementSummaryResponse.Item> items = queryFactory
                .select(Projections.constructor(ProductSettlementSummaryResponse.Item.class,
                        product.name,
                        orderItem.quantity.sum().castToNum(Long.class),
                        history.totalPrice.sum().castToNum(Long.class),
                        history.platformFee.sum().castToNum(Long.class),
                        history.pgFee.sum().castToNum(Long.class),
                        history.vat.sum().castToNum(Long.class),
                        history.amount.sum().castToNum(Long.class)
                ))
                .from(history)
                .join(history.order, order)
                .join(orderItem).on(orderItem.order.eq(order))
                .join(orderItem.product, product)
                .where(history.settlement.id.eq(settlementId))
                .groupBy(product.id, product.name)
                .fetch();

        long totalQuantity = items.stream().mapToLong(ProductSettlementSummaryResponse.Item::totalQuantity).sum();
        long totalPrice = items.stream().mapToLong(ProductSettlementSummaryResponse.Item::totalPrice).sum();
        long platformFee = items.stream().mapToLong(ProductSettlementSummaryResponse.Item::platformFee).sum();
        long pgFee = items.stream().mapToLong(ProductSettlementSummaryResponse.Item::pgFee).sum();
        long vat = items.stream().mapToLong(ProductSettlementSummaryResponse.Item::vat).sum();
        long payoutAmount = items.stream().mapToLong(ProductSettlementSummaryResponse.Item::payoutAmount).sum();

        ProductSettlementSummaryResponse.Summary summary =
                new ProductSettlementSummaryResponse.Summary(totalQuantity, totalPrice, platformFee, pgFee, vat, payoutAmount);

        return new ProductSettlementSummaryResponse(items, summary);
    }
}