package goorm.athena.domain.project.repository.query;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.image.entity.QImage;
import goorm.athena.domain.imageGroup.entity.QImageGroup;
import goorm.athena.domain.project.dto.cursor.ProjectCategoryCursorResponse;
import goorm.athena.domain.project.dto.cursor.ProjectDeadlineCursorResponse;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.res.ProjectCategoryResponse;
import goorm.athena.domain.project.dto.res.ProjectDeadlineResponse;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.project.entity.SortTypeDeadline;
import goorm.athena.domain.project.entity.SortTypeLatest;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectFilterQueryRepository {
    private final JPAQueryFactory queryFactory;

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    public ProjectCategoryCursorResponse getProjectsByCategory(ProjectCursorRequest<?> request,
                                                               Long categoryId,
                                                               SortTypeLatest sortType) {
        if ((sortType.name().startsWith("DEADLINE"))) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;

        // 커서 조건 (startAt < 커서 or (startAt == 커서 and id < 커서Id))
        BooleanBuilder builder = new BooleanBuilder();
        if(categoryId != null) {
            builder.and(project.category.id.eq(categoryId));
        }

        // 커서 조건
        builder.and(ProjectQueryHelper.buildCursorLatest(sortType, request, project));

        List<ProjectCategoryResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectCategoryResponse.class,
                        project.id,
                        image.originalUrl,
                        project.seller.nickname,
                        project.title,
                        project.description,
                        Expressions.numberTemplate(Long.class,
                                "floor(({0} * 100.0) / nullif({1}, 0))",
                                project.totalAmount, project.goalAmount),
                        project.createdAt,
                        project.endAt,
                        Expressions.numberTemplate(Integer.class, "DATEDIFF({0}, CURRENT_DATE)", project.endAt, LocalDateTime.now()),
                        project.views
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.id.eq(imageGroup.id)
                                .and(image.isDefault.isTrue())
                )
                .where(builder)
                .orderBy(ProjectQueryHelper.getSortOrdersLatest(sortType, project).toArray(new OrderSpecifier[0]))
                .limit(request.getSize())
                .fetch();

        BooleanBuilder countCondition = new BooleanBuilder();
        if (categoryId != null) {
            countCondition.and(project.category.id.eq(categoryId));
        }

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .where(countCondition)
                .fetchOne();

        // next cursor 구하기
        ProjectCategoryResponse last = content.isEmpty() ? null : content.get(content.size() - 1);

        return switch (sortType) {
            case LATEST -> ProjectCategoryCursorResponse.of(
                    content,
                    last != null ? last.createdAt() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
            case POPULAR -> ProjectCategoryCursorResponse.of(
                    content,
                    last != null ? last.views() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
            case SUCCESS_RATE -> ProjectCategoryCursorResponse.of(
                    content,
                    last != null ? last.achievementRate() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
        };
    }

    // 마감 기한별 프로젝트 조회 (커서 기반 페이징)
    public ProjectDeadlineCursorResponse getProjectsByDeadline(ProjectCursorRequest<LocalDateTime> request,
                                                               SortTypeDeadline sortTypeDeadline) {
        if (!(sortTypeDeadline.name().startsWith("DEADLINE"))) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(project.endAt.after(LocalDateTime.now())); // 마감되지 않은 프로젝트만


        if (request.cursorValue() != null && request.cursorId() != null) {
            builder.and(
                    project.endAt.gt(request.cursorValue())
                            .or(project.endAt.eq(request.cursorValue())
                                    .and(project.id.gt(request.cursorId())))
            );
        }

        List<ProjectDeadlineResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectDeadlineResponse.class,
                        project.id,
                        image.originalUrl,
                        project.seller.nickname,
                        project.title,
                        project.description,
                        Expressions.numberTemplate(Long.class,
                                "floor(({0} * 100.0) / nullif({1}, 0))",
                                project.totalAmount, project.goalAmount),
                        project.createdAt,
                        project.endAt,
                        Expressions.numberTemplate(Integer.class, "DATEDIFF({0}, CURRENT_DATE)", project.endAt),
                        project.views
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.id.eq(imageGroup.id)
                                .and(image.isDefault.isTrue())
                )
                .where(builder)
                .orderBy(ProjectQueryHelper.getSortOrdersDeadLine(sortTypeDeadline, project).toArray(OrderSpecifier[]::new)) // 마감일 빠른 순
                .limit(request.getSize())
                .fetch();

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .fetchOne();

        return ProjectDeadlineCursorResponse.ofByEndAt(content, totalCount);
    }
}
