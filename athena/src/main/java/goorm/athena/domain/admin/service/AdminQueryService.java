package goorm.athena.domain.admin.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.admin.dto.res.ProjectSummaryResponse;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class AdminQueryService {

    private final JPAQueryFactory queryFactory;

    public Page<ProjectSummaryResponse.Item> findProjectList(String keyword, Pageable pageable) {
        QProject project = QProject.project;
        QUser user = QUser.user;

        BooleanBuilder where = new BooleanBuilder();
        if (keyword != null && !keyword.isBlank()) {
            where.and(project.title.containsIgnoreCase(keyword));
        }

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
                        project.createdAt.stringValue(),   // string으로 변환
                        project.seller.nickname,
                        project.isApproved.stringValue()
                ))
                .from(project)
                .join(project.seller, user)
                .where(where)
                .orderBy(pendingPriority.asc(),
                        pageable.getSort().isSorted()
                                ? pageable.getSort().getOrderFor("createdAt").isAscending()
                                ? project.createdAt.asc() : project.createdAt.desc()
                                : project.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(project.count())
                .from(project)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}