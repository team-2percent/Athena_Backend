package goorm.athena.domain.admin.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import goorm.athena.domain.project.entity.ApprovalStatus;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class AdminQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ProjectSummaryResponse findProjectList(String keyword, Pageable pageable) {
        QProject project = QProject.project;
        QUser user = QUser.user;

        BooleanBuilder where = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            where.and(project.title.containsIgnoreCase(keyword));
        }

        // 정렬 조건 생성 (PENDING 우선 + 전달된 정렬 기준)
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        // PENDING 우선 정렬
        orderSpecifiers.add(
                new CaseBuilder()
                        .when(project.isApproved.eq(ApprovalStatus.PENDING)).then(0)
                        .otherwise(1)
                        .asc()
        );

        // pageable의 sort 조건 추가
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty();
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;

            PathBuilder<?> pathBuilder = new PathBuilder<>(project.getType(), project.getMetadata());
            orderSpecifiers.add(new OrderSpecifier(direction, pathBuilder.get(property)));
        }

        List<ProjectSummaryResponse.Item> content = queryFactory
                .select(Projections.constructor(
                        ProjectSummaryResponse.Item.class,
                        project.id,
                        project.title,
                        project.createdAt.stringValue(),
                        project.seller.nickname,
                        project.isApproved,
                        project.platformPlan.name
                ))
                .from(project)
                .join(project.seller, user)
                .where(where)
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(project.count())
                .from(project)
                .where(where)
                .fetchOne();

        Long pendingCount = queryFactory
                .select(project.count())
                .from(project)
                .where(where.and(project.isApproved.eq(ApprovalStatus.PENDING)))
                .fetchOne();

        int totalPages = (int) Math.ceil((double)(total == null ? 0 : total) / pageable.getPageSize());

        return new ProjectSummaryResponse(
                content,
                new ProjectSummaryResponse.PageInfo(pageable.getPageNumber(), totalPages),
                pendingCount == null ? 0 : pendingCount
        );
    }
}