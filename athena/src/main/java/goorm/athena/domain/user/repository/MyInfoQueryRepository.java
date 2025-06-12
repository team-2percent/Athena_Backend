package goorm.athena.domain.user.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.comment.entity.QComment;
import goorm.athena.domain.image.entity.QImage;
import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.imageGroup.entity.QImageGroup;
import goorm.athena.domain.order.entity.QOrder;
import goorm.athena.domain.orderitem.entity.QOrderItem;
import goorm.athena.domain.product.entity.QProduct;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.project.entity.Status;
import goorm.athena.domain.user.dto.response.MyOrderScrollRequest;
import goorm.athena.domain.user.dto.response.MyOrderScrollResponse;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import goorm.athena.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MyInfoQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final ImageService imageService;

    public MyProjectScrollResponse findMyProjectsByCursor(
            Long userId,
            LocalDateTime cursorCreatedAt,
            Long cursorProjectId,
            int pageSize
    ) {
        QProject project = QProject.project;
        QImage image = QImage.image;
        QImageGroup imageGroup = QImageGroup.imageGroup;

        // ACTIVE = 0, 그 외 = 1
        NumberExpression<Integer> statusOrder = new CaseBuilder()
                .when(project.status.eq(Status.ACTIVE)).then(0)
                .otherwise(1);

        BooleanBuilder whereBuilder = new BooleanBuilder()
                .and(project.seller.id.eq(userId));

        if (cursorCreatedAt != null && cursorProjectId != null) {
            Integer cursorStatusOrder = queryFactory
                    .select(new CaseBuilder()
                            .when(project.status.eq(Status.ACTIVE)).then(0)
                            .otherwise(1))
                    .from(project)
                    .where(project.id.eq(cursorProjectId))
                    .fetchFirst();

            whereBuilder.and(
                    statusOrder.gt(cursorStatusOrder)
                            .or(statusOrder.eq(cursorStatusOrder)
                                    .and(project.createdAt.lt(cursorCreatedAt)
                                            .or(project.createdAt.eq(cursorCreatedAt)
                                                    .and(project.id.lt(cursorProjectId)))))
            );
        }

        List<MyProjectScrollResponse.ProjectPreview> content = queryFactory
                .select(Projections.constructor(MyProjectScrollResponse.ProjectPreview.class,
                        project.id,
                        project.title,
                        project.status.stringValue().eq(Status.COMPLETED.name()),
                        project.createdAt,
                        project.endAt,
                        Expressions.numberTemplate(Long.class,
                                "floor(({0} * 100.0) / nullif({1}, 0))",
                                project.totalAmount, project.goalAmount),
                        image.originalUrl
                ))
                .from(project)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.eq(imageGroup)
                                .and(image.imageIndex.eq(1L))
                )
                .where(whereBuilder)
                .orderBy(
                        statusOrder.asc(),
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

    public MyOrderScrollResponse findOrdersByUserIdWithScroll(Long userId, MyOrderScrollRequest request) {
        QOrder order = QOrder.order;
        QOrderItem orderItem = QOrderItem.orderItem;
        QProduct product = QProduct.product;
        QProject project = QProject.project;
        QUser seller = QUser.user;
        QImage image = QImage.image;
        QImageGroup imageGroup = QImageGroup.imageGroup;
        QComment comment = QComment.comment;

        List<Tuple> results = queryFactory
                .select(
                        order.id,
                        project.id,
                        product.id,
                        project.title,
                        product.name,
                        seller.nickname,
                        image.originalUrl,
                        order.orderedAt,
                        project.endAt,
                        project.totalAmount.multiply(100.0)
                                .divide(project.goalAmount.castToNum(Double.class))
                                .floor()
                                .castToNum(Long.class)
                                .as("achievementRate"),
                        JPAExpressions.selectOne()
                                .from(comment)
                                .where(
                                        comment.project.id.eq(project.id),
                                        comment.user.id.eq(order.user.id)
                                ).exists()
                                .as("hasCommented")
                )
                .from(orderItem)
                .join(orderItem.order, order)
                .join(orderItem.product, product)
                .leftJoin(product.project, project)
                .leftJoin(project.seller, seller)
                .leftJoin(project.imageGroup, imageGroup)
                .leftJoin(image).on(
                        image.imageGroup.eq(imageGroup)
                                .and(image.imageIndex.eq(1L))
                )
                .where(
                        order.user.id.eq(userId)
                                .and(order.status.eq(goorm.athena.domain.order.entity.Status.ORDERED))
                                .and(getCursorCondition(order, request))
                )
                .orderBy(order.orderedAt.desc(), order.id.desc())
                .limit(request.pageSize())
                .fetch();

        List<MyOrderScrollResponse.Item> items = results.stream()
                .map(row -> {
                    Long rate = row.get(9, Long.class);
                    boolean hasCommented = Boolean.TRUE.equals(row.get(10, Boolean.class));
                    String processedImageUrl = imageService.getFullUrl(row.get(image.originalUrl));


                    return new MyOrderScrollResponse.Item(
                            row.get(order.id),
                            row.get(project.id),
                            row.get(product.id),
                            row.get(project.title),
                            row.get(product.name),
                            row.get(seller.nickname),
                            processedImageUrl,
                            row.get(order.orderedAt),
                            row.get(project.endAt),
                            rate,
                            hasCommented
                    );
                })
                .toList();

        if (items.isEmpty()) {
            return new MyOrderScrollResponse(Collections.emptyList(), null, null);
        }

        MyOrderScrollResponse.Item last = items.get(items.size() - 1);
        return new MyOrderScrollResponse(items, last.orderedAt(), last.orderId());
    }

    private BooleanExpression getCursorCondition(QOrder order, MyOrderScrollRequest request) {
        if (request.nextCursorValue() == null || request.nextOrderId() == null) {
            return null;
        }

        return order.orderedAt.lt(request.nextCursorValue())
                .or(order.orderedAt.eq(request.nextCursorValue())
                        .and(order.id.lt(request.nextOrderId())));
    }
}