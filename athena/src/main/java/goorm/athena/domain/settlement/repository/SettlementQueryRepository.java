package goorm.athena.domain.settlement.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.admin.dto.res.SettlementDetailInfoResponse;
import goorm.athena.domain.bankaccount.entity.QBankAccount;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.settlement.dto.res.SettlementSummaryResponse;
import goorm.athena.domain.settlement.entity.QSettlement;
import goorm.athena.domain.settlement.entity.Status;
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
                        settlement.platformFee,
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
                        project.goalAmount,
                        settlement.totalSales,
                        settlement.payOutAmount,
                        settlement.platformFee,
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
}