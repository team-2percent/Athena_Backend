package goorm.athena.domain.project.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.category.entity.QCategory;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.entity.QImage;
import goorm.athena.domain.image.service.ImageQueryService;
import goorm.athena.domain.imageGroup.entity.QImageGroup;
import goorm.athena.domain.project.dto.cursor.*;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.dto.res.ProjectRecentResponse;
import goorm.athena.domain.project.entity.ApprovalStatus;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final ImageQueryService imageQueryService;

    public ProjectDetailDto getProjectDetail(Long projectId) {
        QProject project = QProject.project;
        QCategory category = QCategory.category;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QUser user = QUser.user;

        Project result = queryFactory.selectFrom(project)
                .join(project.category, category).fetchJoin()
                .join(project.seller, user).fetchJoin()
                .join(project.imageGroup, imageGroup).fetchJoin()
                .where(project.id.eq(projectId))
                .fetchOne();

        List<Image> images = imageQueryService.getProjectImages(result.getImageGroup().getId());

        return new ProjectDetailDto(result, result.getCategory(), result.getSeller(), images);
    }

    // 최신 프로젝트 조회 (커서 기반 페이징)
    public ProjectRecentCursorResponse getProjectsByNew(ProjectCursorRequest<LocalDateTime> request) {
        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QImage image = QImage.image;

        // 커서 조건 (startAt < 커서 or (startAt == 커서 and id < 커서Id))
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(project.isApproved.eq(ApprovalStatus.APPROVED));

        if (request.cursorValue() != null && request.cursorId() != null) {
            builder.and(
                    project.createdAt.lt(request.cursorValue())
                            .or(project.createdAt.eq(request.cursorValue())
                                    .and(project.id.lt(request.cursorId())))
            );
        }

        List<ProjectRecentResponse> rawContent = queryFactory
                .select(Projections.constructor(
                        ProjectRecentResponse.class,
                        project.id,
                        image.originalUrl,
                        project.seller.nickname,
                        project.title,
                        project.description,
                        Expressions.numberTemplate(Integer.class,
                                "floor(({0} * 100.0) / nullif({1}, 0))",
                                project.totalAmount, project.goalAmount),
                        project.createdAt,
                        project.endAt,
                                                Expressions.numberTemplate(Integer.class,
                                                                "TIMESTAMPDIFF(DAY, {0}, {1})",
                                                                Expressions.currentDate(), project.endAt)))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.id.eq(imageGroup.id)
                                .and(image.imageIndex.eq(1L))
                )
                .where(builder) // 마지막 프로젝트 ID보다 큰 항목 가져오기
                .orderBy(project.createdAt.desc(), project.id.desc()) // 최신순으로 정렬
                .limit(request.getSize())
                .fetch();

        // 이미지 URL 전처리 후 새 리스트 생성
        List<ProjectRecentResponse> content = rawContent.stream()
                .map(dto -> new ProjectRecentResponse(
                        dto.id(),
                        StringUtils.hasText(dto.imageUrl())
                                ? imageQueryService.getFullUrl(dto.imageUrl().trim())
                                : null,
                        dto.sellerName(),
                        dto.title(),
                        dto.description(),
                        dto.achievementRate(),
                        dto.createdAt(),
                        dto.endAt(),
                        dto.daysLeft()
                ))
                .toList();

        Long totalCount = queryFactory
                .select(project.count())
                .from(project)
                .where(builder)
                .fetchOne();

        // 다음 커서 계산: 마지막 프로젝트 ID를 nextCursor로 반환
        return ProjectRecentCursorResponse.ofByCreatedAt(content, totalCount);// Pageable.unpaged()를 사용하여 페이징 없이 총 수만 반환
    }
}