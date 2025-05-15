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
import goorm.athena.domain.project.entity.SortTypeDeadLine;
import goorm.athena.domain.project.entity.SortTypeLatest;
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

    private List<OrderSpecifier<?>> getSortOrdersDeadLine(SortTypeDeadLine sortTypeDeadLine, QProject project) {
        NumberExpression<Long> successRate = project.totalAmount.multiply(100.0)
                .divide(project.goalAmount.doubleValue());

        return switch (sortTypeDeadLine) {
            case DEADLINE -> List.of(project.endAt.asc(), project.id.asc());
            case DEADLINE_POPULAR -> List.of(project.endAt.asc(), project.views.desc(), project.id.asc());
            case DEADLINE_SUCCESS_RATE -> List.of(project.endAt.asc(), successRate.desc(), project.id.asc());
            case DEADLINE_RECOMMENDED -> List.of(project.endAt.asc(),
                    Expressions.numberTemplate(Double.class, "function('rand')").asc(), project.id.asc());

        };
    }

    private List<OrderSpecifier<?>> getSortOrdersLatest(SortTypeLatest sortType, QProject project) {
        NumberExpression<Long> successRate = project.totalAmount.multiply(100.0)
                .divide(project.goalAmount.doubleValue());

        return switch (sortType) {

            case LATEST -> List.of(project.createdAt.desc(), project.id.desc());
            case POPULAR -> List.of(project.views.desc(), project.id.desc());
            case SUCCESS_RATE -> List.of(successRate.desc(), project.id.desc());
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
                        image.originalUrl,
                        project.seller.nickname,
                        project.title,
                        project.description,
                        Expressions.numberTemplate(Long.class,
                                "floor(({0} * 100.0) / nullif({1}, 0))",
                                project.totalAmount, project.goalAmount),
                        project.createdAt,
                        project.endAt,
                        Expressions.numberTemplate(Integer.class, "DATEDIFF({0}, CURRENT_DATE)", project.endAt)
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
    public ProjectFilterCursorResponse<?> getProjectsByCategory(ProjectCursorRequest<?> request,
                                                                                Long categoryId,
                                                                                SortTypeLatest sortType) {
        if ((sortType.name().startsWith("DEADLINE"))) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;
        QImage imageSub = new QImage("imageSub");

        // 커서 조건 (startAt < 커서 or (startAt == 커서 and id < 커서Id))
        BooleanBuilder builder = new BooleanBuilder();
        if(categoryId != null) {
            builder.and(project.category.id.eq(categoryId));
        }

        // 커서 조건
        switch (sortType) {
            case LATEST -> {
                String rawCursor = (String) request.cursorValue();
                LocalDateTime cursorCreatedAt = rawCursor != null ? LocalDateTime.parse(rawCursor) : null;
                Long cursorId = request.cursorId();
                if (cursorCreatedAt != null && cursorId != null) {
                    builder.and(project.createdAt.lt(cursorCreatedAt)
                            .or(project.createdAt.eq(cursorCreatedAt).and(project.id.lt(cursorId))));
                }
            }

            case POPULAR -> {
                String rawCursor = (String) request.cursorValue();
                Long cursorViews = rawCursor != null ? Long.parseLong(rawCursor) : null;
                Long cursorId = request.cursorId();
                if (cursorViews != null && cursorId != null) {
                    builder.and(project.views.lt(cursorViews)
                            .or(project.views.eq(cursorViews).and(project.id.lt(cursorId))));
                }
            }

            case SUCCESS_RATE -> {
                String rawCursor = (String) request.cursorValue();
                Double cursorRate = rawCursor != null ? Double.parseDouble(rawCursor) : null;
                Long cursorId = request.cursorId();
                if (cursorRate != null && cursorId != null) {
                    NumberExpression<Long> successRate =
                            project.totalAmount.multiply(100.0).divide(project.goalAmount.doubleValue());

                    builder.and(successRate.lt(cursorRate)
                            .or(successRate.eq(Expressions.constant(cursorRate)).and(project.id.lt(cursorId))));
                }
            }

            default -> {
                throw new CustomException(ErrorCode.COUPON_NOT_FOUND);
            }
        }

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
                                .and(image.id.eq(
                                        JPAExpressions.select(imageSub.id)
                                                .from(imageSub)
                                                .where(imageSub.imageGroup.id.eq(imageGroup.id)
                                                        .and(imageSub.isDefault.isTrue()))
                                ))
                )
                .where(builder)
                .orderBy(getSortOrdersLatest(sortType, project).toArray(new OrderSpecifier[0]))
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
            case LATEST -> ProjectFilterCursorResponse.of(
                    content,
                    last != null ? last.createdAt() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
            case POPULAR -> ProjectFilterCursorResponse.of(
                    content,
                    last != null ? last.views() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
            case SUCCESS_RATE -> ProjectFilterCursorResponse.of(
                    content,
                    last != null ? last.achievementRate() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
        };
    }

    // 마감 기한별 프로젝트 조회 (커서 기반 페이징)
    public ProjectCursorResponse<ProjectDeadLineResponse> getProjectsByDeadline(ProjectCursorRequest<LocalDateTime> request,
                                                                                SortTypeDeadLine sortTypeDeadLine) {
        if (!(sortTypeDeadLine.name().startsWith("DEADLINE"))) {
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
                                .and(image.id.eq(
                                        JPAExpressions.select(imageSub.id)
                                                .from(imageSub)
                                                .where(imageSub.imageGroup.id.eq(imageGroup.id)
                                                        .and(imageSub.isDefault.isTrue()))
                                ))
                )
                .where(builder)
                .orderBy(getSortOrdersDeadLine(sortTypeDeadLine, project).toArray(OrderSpecifier[]::new)) // 마감일 빠른 순
                .limit(request.getSize())
                .fetch();

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .fetchOne();

        return ProjectCursorResponse.ofByEndAt(content, totalCount);
    }

    // 검색 기반 페이지 조회
    public ProjectFilterCursorResponse<ProjectSearchResponse> searchProjects(ProjectCursorRequest<?> request,
                                                                               String searchTerm,
                                                                               SortTypeLatest sortType) {
        if ((sortType.name().startsWith("DEADLINE"))) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;
        QImage imageSub = new QImage("imageSub");

        // 커서 조건 (startAt < 커서 or (startAt == 커서 and id < 커서Id))
        BooleanBuilder builder = new BooleanBuilder();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            builder.and(project.title.containsIgnoreCase(searchTerm));
        }

        // 커서 조건
        switch (sortType) {
            case LATEST -> {
                String rawCursor = (String) request.cursorValue();
                LocalDateTime cursorCreatedAt = rawCursor != null ? LocalDateTime.parse(rawCursor) : null;
                Long cursorId = request.cursorId();
                if (cursorCreatedAt != null && cursorId != null) {
                    builder.and(project.createdAt.lt(cursorCreatedAt)
                            .or(project.createdAt.eq(cursorCreatedAt).and(project.id.lt(cursorId))));
                }
            }

            case POPULAR -> {
                String rawCursor = (String) request.cursorValue();
                Long cursorViews = rawCursor != null ? Long.parseLong(rawCursor) : null;
                Long cursorId = request.cursorId();
                if (cursorViews != null && cursorId != null) {
                    builder.and(project.views.lt(cursorViews)
                            .or(project.views.eq(cursorViews).and(project.id.lt(cursorId))));
                }
            }

            case SUCCESS_RATE -> {
                String rawCursor = (String) request.cursorValue();
                Double cursorRate = rawCursor != null ? Double.parseDouble(rawCursor) : null;
                Long cursorId = request.cursorId();
                if (cursorRate != null && cursorId != null) {
                    NumberExpression<Long> successRate =
                            project.totalAmount.multiply(100.0).divide(project.goalAmount.doubleValue());

                    builder.and(successRate.lt(cursorRate)
                            .or(successRate.eq(Expressions.constant(cursorRate)).and(project.id.lt(cursorId))));
                }
            }

            default -> {
                throw new CustomException(ErrorCode.COUPON_NOT_FOUND);
            }
        }

        // 나머지 fetch, join, 정렬 등 쿼리 작성
        List<ProjectSearchResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectSearchResponse.class,
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
                .orderBy(getSortOrdersLatest(sortType, project).toArray(new OrderSpecifier[0]))
                .limit(request.getSize())
                .fetch();

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .where(project.title.containsIgnoreCase(searchTerm))
                .fetchOne();

        // next cursor 구하기
        ProjectSearchResponse last = content.isEmpty() ? null : content.get(content.size() - 1);

        return switch (sortType) {
            case LATEST -> ProjectFilterCursorResponse.ofSearch(
                    content,
                    last != null ? last.createdAt() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
            case POPULAR -> ProjectFilterCursorResponse.ofSearch(
                    content,
                    last != null ? last.views() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
            case SUCCESS_RATE -> ProjectFilterCursorResponse.ofSearch(
                    content,
                    last != null ? last.achievementRate() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
        };
    }
}