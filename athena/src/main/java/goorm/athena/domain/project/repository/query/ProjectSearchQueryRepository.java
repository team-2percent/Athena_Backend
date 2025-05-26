package goorm.athena.domain.project.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.image.entity.QImage;
import goorm.athena.domain.imageGroup.entity.QImageGroup;
import goorm.athena.domain.project.dto.cursor.ProjectSearchCursorResponse;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.res.ProjectSearchResponse;
import goorm.athena.domain.project.entity.ApprovalStatus;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.project.entity.SortTypeLatest;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectSearchQueryRepository {
    private final JPAQueryFactory queryFactory;


    // 검색 기반 페이지 조회
    public ProjectSearchCursorResponse searchProjects(ProjectCursorRequest<?> request,
                                                      String searchTerm,
                                                      SortTypeLatest sortType) {
        if ((sortType.name().startsWith("DEADLINE"))) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;

        // 커서 조건 (startAt < 커서 or (startAt == 커서 and id < 커서Id))
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(project.isApproved.eq(ApprovalStatus.APPROVED));

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            builder.and(project.title.containsIgnoreCase(searchTerm));
        }

        // 커서 조건
        builder.and(ProjectQueryHelper.buildCursorLatest(sortType, request, project));

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
                .orderBy(ProjectQueryHelper.getSortOrdersLatest(sortType, project).toArray(new OrderSpecifier[0]))
                .limit(request.getSize())
                .fetch();

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .where(builder)
                .fetchOne();

        // next cursor 구하기
        ProjectSearchResponse last = content.isEmpty() ? null : content.get(content.size() - 1);

        return switch (sortType) {
            case LATEST -> ProjectSearchCursorResponse.ofSearch(
                    content,
                    last != null ? last.createdAt() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
            case POPULAR -> ProjectSearchCursorResponse.ofSearch(
                    content,
                    last != null ? last.views() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
            case SUCCESS_RATE -> ProjectSearchCursorResponse.ofSearch(
                    content,
                    last != null ? last.achievementRate() : null,
                    last != null ? last.id() : null,
                    totalCount
            );
        };
    }
}
