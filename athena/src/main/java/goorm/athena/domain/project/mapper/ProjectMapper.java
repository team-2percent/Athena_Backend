package goorm.athena.domain.project.mapper;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.res.*;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.dto.response.UserDetailResponse;
import goorm.athena.domain.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    // CreateRequest -> Entity
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "views", ignore = true)
    @Mapping(target = "seller", source = "seller")
    @Mapping(target = "imageGroup", source = "imageGroup")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "bankAccount", source = "bankAccount")
    @Mapping(target = "platformPlan", source = "platformPlan")
    @Mapping(target = "contentMarkdown", source = "convertedMarkdown")
    @Mapping(target = "totalAmount", constant = "0L")
    Project toEntity(ProjectCreateRequest request,
                     User seller,
                     ImageGroup imageGroup,
                     Category category,
                     BankAccount bankAccount,
                     PlatformPlan platformPlan,
                     String convertedMarkdown);

    // Entity -> ProjectIdResponse
    @Mapping(target = "projectId", source = "id")
    @Mapping(target = "imageGroupId", source = "imageGroup.id")
    ProjectIdResponse toCreateDto(Project project);


    // Entity -> ProjectDetailResponse (프로젝트 상세 페이지 조회)
    @Mapping(target = "id", source = "project.id")
    @Mapping(target = "markdown", source = "project.contentMarkdown")
    @Mapping(target = "planName", source = "project.platformPlan.name")
    @Mapping(target = "category", source = "category.categoryName")
    @Mapping(target = "imageUrls", source = "imageUrls")
    @Mapping(target = "sellerResponse", source = "userDetailResponse")
    @Mapping(target = "productResponses", source = "productResponses")
    ProjectDetailResponse toDetailDto(Project project,
                                      Category category,
                                      List<String> imageUrls,
                                      UserDetailResponse userDetailResponse,
                                      List<ProductResponse> productResponses);

    // Entity -> ProjectTopViewResponse
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "sellerName", source = "project.seller.nickname")
    @Mapping(target = "achievementRate", expression = "java((project.getTotalAmount() == 0 || project.getGoalAmount() == 0) ? 0L : (project.getTotalAmount() * 100) / project.getGoalAmount())")
    @Mapping(target = "imageUrl", source = "imageUrl")
    ProjectTopViewResponse toTopViewResponse(Project project, String imageUrl);

    // Entity -> ProjectCategoryTopViewResponse
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "items", source = "responses")
    ProjectCategoryTopViewResponse toCategoryTopView(Category category, List<ProjectTopViewResponse> responses);
}
