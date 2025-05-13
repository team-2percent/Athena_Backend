package goorm.athena.domain.project.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.image.entity.QImage;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.QImageGroup;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.res.ProjectAllResponse;
import goorm.athena.domain.project.dto.res.ProjectCategoryResponse;
import goorm.athena.domain.project.dto.res.ProjectCursorResponse;
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

    // 최신 프로젝트 조회 (커서 기반 페이징)
    public ProjectCursorResponse<ProjectAllResponse> getProjectsByNew(ProjectCursorRequest<LocalDateTime> request) {
        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;


        // 커서 조건 (startAt < 커서 or (startAt == 커서 and id < 커서Id))
        BooleanBuilder builder = new BooleanBuilder();

        if (request.cursorValue() != null && request.cursorId() != null) {
            builder.and(
                    project.startAt.lt(request.cursorValue())
                            .or(project.startAt.eq(request.cursorValue())
                                    .and(project.id.lt(request.cursorId())))
            );
        }

        // 서브쿼리: imageGroup 별로 가장 id가 작은 이미지
        QImage imageSub = new QImage("imageSub");

        List<ProjectAllResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectAllResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        project.goalAmount,
                        project.totalAmount,
                        project.startAt,
                        project.endAt,
                        image.originalUrl
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.id.eq(imageGroup.id)
                                .and(image.id.eq(
                                        JPAExpressions
                                                .select(imageSub.id.min())
                                                .from(imageSub)
                                                .where(imageSub.imageGroup.id.eq(imageGroup.id))
                                ))
                )
                .where(builder) // 마지막 프로젝트 ID보다 큰 항목 가져오기
                .orderBy(project.startAt.desc(), project.id.desc()) // 최신순으로 정렬
                .limit(request.getSize())
                .fetch();


        // 다음 커서 계산: 마지막 프로젝트 ID를 nextCursor로 반환
        return ProjectCursorResponse.of(content);// Pageable.unpaged()를 사용하여 페이징 없이 총 수만 반환
    }

    // 카테고리별 프로젝트 조회 (커서 기반 페이징)
    public ProjectCursorResponse<ProjectCategoryResponse> getProjectsByCategory(Long categoryId, SortType sortType, Long lastProjectId, int pageSize) {
        if (sortType == SortType.DEADLINE) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;

        List<ProjectCategoryResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectCategoryResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        project.goalAmount,
                        project.totalAmount,
                        project.startAt,
                        project.endAt,
                        image.originalUrl
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(image.imageGroup.id.eq(imageGroup.id))
                .where(project.category.id.eq(categoryId))
                .where(lastProjectId == null ? null : project.id.gt(lastProjectId)) // 마지막 프로젝트 ID보다 큰 항목 가져오기
                .orderBy(getSortOrder(sortType, project))
                .limit(pageSize)
                .fetch();

        long total = queryFactory
                .select(project.count())
                .from(project)
                .where(project.category.id.eq(categoryId))
                .where(lastProjectId == null ? null : project.id.gt(lastProjectId)) // 마지막 프로젝트 ID보다 큰 항목의 갯수
                .fetchOne();

        return ProjectCursorResponse.of(content);
    }

    // 마감 기한별 프로젝트 조회 (커서 기반 페이징)
    public ProjectCursorResponse<ProjectDeadLineResponse> getProjectsByDeadline(SortType sortType, Long lastProjectId, int pageSize) {
        if (sortType == SortType.LATEST) {
            throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }

        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;

        List<ProjectDeadLineResponse> content = queryFactory
                .select(Projections.constructor(
                        ProjectDeadLineResponse.class,
                        project.id,
                        project.title,
                        project.views,
                        project.goalAmount,
                        project.totalAmount,
                        project.startAt,
                        project.endAt,
                        image.originalUrl
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(image.imageGroup.id.eq(imageGroup.id))
                .where(project.endAt.after(LocalDateTime.now())) // 마감 임박: 현재 시간 이후 마감된 프로젝트
                .where(lastProjectId == null ? null : project.id.gt(lastProjectId)) // 마지막 프로젝트 ID보다 큰 항목 가져오기
                .orderBy(getSortOrder(sortType, project))
                .limit(pageSize)
                .fetch();

        long total = queryFactory
                .select(project.count())
                .from(project)
                .where(project.endAt.after(LocalDateTime.now()))
                .where(lastProjectId == null ? null : project.id.gt(lastProjectId)) // 마지막 프로젝트 ID보다 큰 항목의 갯수
                .fetchOne();

        // 다음 커서 계산: 마지막 프로젝트 ID를 nextCursor로 반환
        Long nextCursor = content.isEmpty() ? null : content.get(content.size() - 1).id();

        return ProjectCursorResponse.of(content);
    }
}
