package goorm.athena.domain.project.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.image.entity.QImage;
import goorm.athena.domain.imageGroup.entity.QImageGroup;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.project.entity.SortType;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepository {
    private final JPAQueryFactory queryFactory;

    private List<OrderSpecifier<?>> getSortOrders(SortType sortType, QProject project) {
        NumberExpression<Long> successRate = project.totalAmount.multiply(100.0)
                .divide(project.goalAmount.doubleValue());

        return switch (sortType) {
            case DEADLINE -> List.of(project.endAt.asc(), project.id.asc());
            case DEADLINE_POPULAR -> List.of(project.endAt.asc(), project.views.desc(), project.id.asc());
            case DEADLINE_SUCCESS_RATE -> List.of(project.endAt.asc(), successRate.desc(), project.id.asc());
            case DEADLINE_RECOMMENDED -> List.of(project.endAt.asc(),
                    Expressions.numberTemplate(Double.class, "function('rand')").asc(), project.id.asc());

            case LATEST -> List.of(project.createdAt.desc(), project.id.asc());
            case POPULAR -> List.of(project.views.desc(), project.id.asc());
            case SUCCESS_RATE -> List.of(successRate.desc(), project.id.asc());
            case RECOMMENDED -> List.of(Expressions.numberTemplate(Double.class, "function('rand')").asc(), project.id.asc());
        };
    }

    // 최신 프로젝트 조회 (커서 기반 페이징)
    public ProjectCursorResponse<ProjectRecentResponse> getProjectsByNew(ProjectCursorRequest<LocalDateTime> request) {
        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;

        // 커서 조건 (startAt < 커서 or (startAt == 커서 and id < 커서Id))
        BooleanBuilder builder = new BooleanBuilder();

        if (request.cursorValue() != null && request.cursorId() != null) {
            builder.and(
                    project.createdAt.lt(request.cursorValue())
                            .or(project.createdAt.eq(request.cursorValue())
                                    .and(project.id.lt(request.cursorId())))
            );
        }

        // 서브쿼리: imageGroup 별로 가장 id가 작은 이미지
        QImage imageSub = new QImage("imageSub");

        List<ProjectRecentResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectRecentResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        Expressions.numberTemplate(Long.class,
                                "floor(({0} * 100.0) / nullif({1}, 0))",
                                project.totalAmount, project.goalAmount),
                        project.startAt,
                        project.endAt,
                        project.createdAt,
                        image.originalUrl
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.id.eq(imageGroup.id)
                                .and(image.id.eq(
                                        JPAExpressions
                                                .select(imageSub.id)
                                                .from(imageSub)
                                                .where(imageSub.imageGroup.id.eq(imageGroup.id)
                                                        .and(imageSub.isDefault.isTrue()))
                                ))
                )
                .where(builder) // 마지막 프로젝트 ID보다 큰 항목 가져오기
                .orderBy(project.createdAt.desc(), project.id.desc()) // 최신순으로 정렬
                .limit(request.getSize())
                .fetch();

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .fetchOne();

        // 다음 커서 계산: 마지막 프로젝트 ID를 nextCursor로 반환
        return ProjectCursorResponse.ofByCreatedAt(content, totalCount);// Pageable.unpaged()를 사용하여 페이징 없이 총 수만 반환
    }

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    public ProjectCursorResponse<ProjectCategoryResponse> getProjectsByCategory(ProjectCursorRequest<LocalDateTime> request,
                                                                                Long categoryId,
                                                                                SortType sortType) {
        if ((sortType.name().startsWith("DEADLINE"))) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;

        // 커서 조건 (startAt < 커서 or (startAt == 커서 and id < 커서Id))
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(project.category.id.eq(categoryId));

        if (request.cursorValue() != null && request.cursorId() != null) {
            builder.and(
                    project.createdAt.lt(request.cursorValue())
                            .or(project.createdAt.eq(request.cursorValue())
                                    .and(project.id.lt(request.cursorId())))
            );
        }

        // 서브쿼리: imageGroup 별로 가장 id가 작은 이미지
        QImage imageSub = new QImage("imageSub");

        List<ProjectCategoryResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectCategoryResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        Expressions.numberTemplate(Long.class,
                                "floor(({0} * 100.0) / nullif({1}, 0))",
                                project.totalAmount, project.goalAmount),
                        project.startAt,
                        project.endAt,
                        project.createdAt,
                        image.originalUrl
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.id.eq(imageGroup.id)
                                .and(image.id.eq(
                                        JPAExpressions
                                                .select(imageSub.id)
                                                .from(imageSub)
                                                .where(imageSub.imageGroup.id.eq(imageGroup.id)
                                                        .and(imageSub.isDefault.isTrue()))
                                ))
                )
                .where(builder)
                .orderBy(getSortOrders(sortType, project).toArray(OrderSpecifier[]::new))
                .limit(request.getSize())
                .fetch();

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .where(project.category.id.eq(categoryId))
                .fetchOne();

        return ProjectCursorResponse.ofByStartAt(content, totalCount);
    }

    // 마감 기한별 프로젝트 조회 (커서 기반 페이징)
    public ProjectCursorResponse<ProjectDeadLineResponse> getProjectsByDeadline(ProjectCursorRequest<LocalDateTime> request,
                                                                                SortType sortType) {
        if (!(sortType.name().startsWith("DEADLINE"))) {
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

        QImage imageSub = new QImage("imageSub");

        List<ProjectDeadLineResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectDeadLineResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        Expressions.numberTemplate(Long.class,
                                "floor(({0} * 100.0) / nullif({1}, 0))",
                                project.totalAmount, project.goalAmount),
                        project.startAt,
                        project.endAt,
                        image.originalUrl
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.id.eq(imageGroup.id)
                                .and(image.id.eq(
                                        JPAExpressions.select(imageSub.id)
                                                .from(imageSub)
                                                .where(imageSub.imageGroup.id.eq(imageGroup.id)
                                                        .and(imageSub.isDefault.isTrue()))
                                ))
                )
                .where(builder)
                .orderBy(getSortOrders(sortType, project).toArray(OrderSpecifier[]::new)) // 마감일 빠른 순
                .limit(request.getSize())
                .fetch();

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .fetchOne();

        return ProjectCursorResponse.ofByEndAt(content, totalCount);
    }

    // 검색 기반 페이지 조회
    public ProjectSearchCursorResponse<ProjectSearchResponse> searchProjects(ProjectCursorRequest<String> request,
                                                                               String searchTerm,
                                                                               SortType sortType) {
        if ((sortType.name().startsWith("DEADLINE"))) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;

            // 커서 조건 (startAt < 커서 or (startAt == 커서 and id < 커서Id))
        BooleanBuilder builder = new BooleanBuilder();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            builder.and(project.title.containsIgnoreCase(searchTerm));
        }

        if (request.cursorId() != null) {
            builder.and(project.id.gt(request.cursorId()));
        }

        // 나머지 fetch, join, 정렬 등 쿼리 작성
        List<ProjectSearchResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectSearchResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        Expressions.numberTemplate(Long.class,
                                "floor(({0} * 100.0) / nullif({1}, 0))",
                                project.totalAmount, project.goalAmount),
                        project.startAt,
                        project.endAt,
                        project.createdAt,
                        image.originalUrl
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.id.eq(imageGroup.id)
                                .and(image.isDefault.isTrue())
                )
                .where(builder)
                .orderBy(getSortOrders(sortType, project).toArray(OrderSpecifier[]::new))
                .limit(request.getSize())
                .fetch();

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .where(project.title.containsIgnoreCase(searchTerm))
                .fetchOne();

        return ProjectSearchCursorResponse.ofBySearch(content, searchTerm, totalCount);
    }
}