package goorm.athena.domain.project.mapper;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.req.ProjectUpdateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.dto.res.ProjectTopViewResponse;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;

public class ProjectMapper {
    // ProjectCreateRequest(Dto) -> Entity

    public static Project toEntity(ProjectCreateRequest request, User seller, ImageGroup imageGroup, Category category) {
        return Project.builder()
                .seller(seller)
                .imageGroup(imageGroup)
                .category(category)
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

    // Entity -> ProjectTopViewResponse(Dto)
    public static ProjectTopViewResponse toTopViewResponse(Project project, String imageUrl){
        return new ProjectTopViewResponse(
                project.getId(),
                project.getSeller().getNickname(),
                project.getTitle(),
                project.getCategory().getId()
        );
    }
}
