package goorm.athena.domain.project.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.project.dto.res.ProjectCategoryResponse;
import goorm.athena.domain.project.dto.res.ProjectDeadLineResponse;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.project.entity.SortType;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectQueryService {
    private final JPAQueryFactory queryFactory;

    public Page<ProjectCategoryResponse> getProjectsByCategoryId(Long categoryId, SortType sortType, Pageable pageable){
        if (sortType == SortType.DEADLINE) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;

        List<ProjectCategoryResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectCategoryResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        project.goalAmount,
                        project.totalAmount,
                        project.startAt,
                        project.endAt
                ))
                .from(project)
                .where(project.category.id.eq(categoryId))
                .orderBy(getSortOrder(sortType, project))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(project.count())
                .from(project)
                .where(project.category.id.eq(categoryId))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    public Page<ProjectDeadLineResponse> getProjectsByDeadline(SortType sortType, Pageable pageable) {
        if (sortType == SortType.LATEST) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;

        List<ProjectDeadLineResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectDeadLineResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        project.goalAmount,
                        project.totalAmount,
                        project.startAt,
                        project.endAt
                ))
                .from(project)
                .where(project.endAt.after(LocalDateTime.now())) // 마감 임박: 현재 시간 이후 마감된 프로젝트
                .orderBy(getSortOrder(sortType, project))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(project.count())
                .from(project)
                .where(project.endAt.after(LocalDateTime.now()))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    // 검색 필터링 추후 리팩터링 예정
    public Page<ProjectCategoryResponse> searchProjects(String searchTerm, SortType sortType, Pageable pageable) {
        QProject project = QProject.project;

        List<ProjectCategoryResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectCategoryResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        project.goalAmount,
                        project.totalAmount
                ))
                .from(project)
                .where(project.title.containsIgnoreCase(searchTerm)) // 검색어로 제목 필터링
                .orderBy(getSortOrder(sortType, project)) // 정렬 처리
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(project.count())
                .from(project)
                .where(project.title.containsIgnoreCase(searchTerm))
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private OrderSpecifier<?> getSortOrder(SortType sortType, QProject project) {
        return switch (sortType) {
            case DEADLINE -> project.endAt.asc(); // 마감순
            case LATEST -> project.startAt.desc(); // 최근순
            case RECOMMENDED -> Expressions.numberTemplate(Double.class, "function('rand')").asc(); // 추천은 아직 사용되지 않음
            case POPULAR -> project.views.desc(); // 조회수 순
            case SUCCESS_RATE -> project.totalAmount.multiply(100.0)
                    .divide(project.goalAmount.doubleValue()).desc(); // 성공률 조회
        };
    }
}
