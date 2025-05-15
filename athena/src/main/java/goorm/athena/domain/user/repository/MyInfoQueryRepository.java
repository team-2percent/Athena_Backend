package goorm.athena.domain.user.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MyInfoQueryRepository {

    private final JPAQueryFactory queryFactory;

    public MyProjectScrollResponse findMyProjectsByCursor(
            Long userId,
            LocalDateTime cursorCreatedAt,
            Long cursorProjectId,
            int pageSize
    ) {
        QProject project = QProject.project;

        BooleanBuilder whereBuilder = new BooleanBuilder()
                .and(project.seller.id.eq(userId));

        // 커서 조건
        if (cursorCreatedAt != null && cursorProjectId != null) {
            whereBuilder.and(
                    project.createdAt.lt(cursorCreatedAt)
                            .or(project.createdAt.eq(cursorCreatedAt).and(project.id.lt(cursorProjectId)))
            );
        }

        List<MyProjectScrollResponse.ProjectPreview> content = queryFactory
                .select(Projections.constructor(MyProjectScrollResponse.ProjectPreview.class,
                        project.id,
                        project.title,
                        project.status.eq(goorm.athena.domain.project.entity.Status.COMPLETED),
                        project.createdAt
                ))
                .from(project)
                .where(whereBuilder)
                .orderBy(
                        new CaseBuilder()
                                .when(project.status.eq(goorm.athena.domain.project.entity.Status.ACTIVE)).then(0)
                                .otherwise(1).asc(),
                        project.createdAt.desc(),
                        project.id.desc()
                )
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = content.size() > pageSize;
        if (hasNext) {
            content = content.subList(0, pageSize);
        }

        LocalDateTime nextCursorCreatedAt = hasNext ? content.get(content.size() - 1).createdAt() : null;
        Long nextProjectId = hasNext ? content.get(content.size() - 1).projectId() : null;

        return new MyProjectScrollResponse(content, nextCursorCreatedAt, nextProjectId);
    }
}