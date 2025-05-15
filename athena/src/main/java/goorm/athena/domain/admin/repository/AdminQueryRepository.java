package goorm.athena.domain.admin.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import goorm.athena.domain.project.entity.ApprovalStatus;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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

        // PENDING 우선 정렬
        NumberExpression<Integer> pendingPriority = Expressions.numberTemplate(
                Integer.class,
                "case when {0} = 'PENDING' then 0 else 1 end",
                project.isApproved
        );

        List<ProjectSummaryResponse.Item> content = queryFactory
                .select(Projections.constructor(
                        ProjectSummaryResponse.Item.class,
                        project.id,
                        project.title,
                        project.createdAt.stringValue(),
                        project.seller.nickname,
                        project.isApproved.stringValue()
                ))
                .from(project)
                .join(project.seller, user)
                .where(where)
                .orderBy(
                        new CaseBuilder()
                                .when(project.isApproved.eq(ApprovalStatus.PENDING)).then(0)
                                .otherwise(1)
                                .asc(),
                        project.createdAt.desc()
                )
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