package goorm.athena.domain.project.mapper;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.dto.response.UserDetailResponse;
import goorm.athena.domain.user.entity.User;

import java.util.List;
import java.util.Map;

public class ProjectMapper {
    // ProjectCreateRequest(Dto) -> Entity
    // convertedMarkdown parameter 추가 예정
    public static Project toEntity(ProjectCreateRequest request, User seller,
                                   ImageGroup imageGroup, Category category, BankAccount bankAccount) {
        return Project.builder()
                .seller(seller)
                .imageGroup(imageGroup)
                .category(category)
                .bankAccount(bankAccount)
                .title(request.title())
                .description(request.description())
                .goalAmount(request.goalAmount())
                .totalAmount(0L)
                .contentMarkdown(request.contentMarkdown())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .shippedAt(request.shippedAt())
                .build();
    }

    // Entity -> ProjectIdResponse(Dto)
    public static ProjectIdResponse toCreateDto(Project project){
        return ProjectIdResponse.builder()
                .projectId(project.getId())
                .imageGroupId(project.getImageGroup().getId())
                .build();
    }

    // Entity -> ProjectDetailResponse (프로젝트 상세 페이지 조회)
    public static ProjectDetailResponse toDetailDto(Project project, Category category, List<String> imageUrls,
                                                    UserDetailResponse userDetailResponse, List<ProductResponse> productResponses){
        return new ProjectDetailResponse(
                project.getId(),
                category.getCategoryName(),
                project.getTitle(),
                project.getDescription(),
                project.getGoalAmount(),
                project.getTotalAmount(),
                project.getContentMarkdown(),
                project.getStartAt(),
                project.getEndAt(),
                project.getShippedAt(),
                project.getCreatedAt(),
                imageUrls,
                userDetailResponse,
                productResponses,
                project.getStatus(),
                project.getPlatformPlan().getName()
        );
    }

    // Entity -> ProjectTopViewResponse(Dto)
    public static ProjectTopViewResponse toTopViewResponse(Project project, String imageUrl){
        return new ProjectTopViewResponse(
                project.getSeller().getNickname(),
                project.getTitle(),
                project.getDescription(),
                imageUrl,
                (project.getTotalAmount() * 100) / project.getTotalAmount(),
                project.getId()
        );
    }


    public static ProjectCategoryTopViewResponse toCategoryTopView(Category category, List<ProjectTopViewResponse> responses){
        return new ProjectCategoryTopViewResponse(category.getId(), category.getCategoryName(), responses);
    }
}
