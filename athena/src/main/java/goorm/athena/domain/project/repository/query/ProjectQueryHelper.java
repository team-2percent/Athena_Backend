package goorm.athena.domain.project.repository.query;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import goorm.athena.domain.project.dto.req.ProjectCursorRequest;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.project.entity.SortTypeDeadline;
import goorm.athena.domain.project.entity.SortTypeLatest;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

// 세부 필터 정렬을 위한 클래스 (유틸리티)
public class ProjectQueryHelper {

    // 마감순 세부필터 정렬 (orderBy)
    public static List<OrderSpecifier<?>> getSortOrdersDeadLine(SortTypeDeadline sortTypeDeadline, QProject project) {
        NumberExpression<Long> successRate = project.totalAmount.multiply(100.0)
                .divide(project.goalAmount.doubleValue());

        return switch (sortTypeDeadline) {
            case DEADLINE -> List.of(project.endAt.asc(), project.id.asc());
            case DEADLINE_POPULAR -> List.of(project.endAt.asc(), project.views.desc(), project.id.asc());
            case DEADLINE_SUCCESS_RATE -> List.of(project.endAt.asc(), successRate.desc(), project.id.asc());
            case DEADLINE_RECOMMENDED -> List.of(project.endAt.asc(),
                    Expressions.numberTemplate(Double.class, "function('rand')").asc(), project.id.asc());

        };
    }

    // 최신순 세부필터 정렬 (orderBy)
    public static List<OrderSpecifier<?>> getSortOrdersLatest(SortTypeLatest sortType, QProject project) {
        NumberExpression<Long> successRate = project.totalAmount.multiply(100.0)
                .divide(project.goalAmount.doubleValue());

        return switch (sortType) {
            case LATEST -> List.of(project.createdAt.desc(), project.id.desc());
            case POPULAR -> List.of(project.views.desc(), project.id.desc());
            case SUCCESS_RATE -> List.of(successRate.desc(), project.id.desc());
        };
    }

    // 최신순 기준의 조회 조건 (where)
    public static BooleanBuilder buildCursorLatest(SortTypeLatest sortType, ProjectCursorRequest<?> request,
                                                   QProject project){
        BooleanBuilder builder = new BooleanBuilder();

        switch (sortType) {
            case LATEST -> {
                LocalDateTime cursorCreatedAt = request.cursorValue() instanceof String rawCursorStr
                    ? LocalDateTime.parse(rawCursorStr)
                    : request.cursorValue() instanceof LocalDateTime rawCursorLocalDateTime
                        ? rawCursorLocalDateTime
                        : null;
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

            default -> throw new CustomException(ErrorCode.INVALID_PROJECT_ORDER);
        }
        return builder;
    }
}
