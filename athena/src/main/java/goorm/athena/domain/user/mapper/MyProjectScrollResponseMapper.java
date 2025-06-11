package goorm.athena.domain.user.mapper;

import goorm.athena.domain.image.service.ImageService;
import goorm.athena.domain.user.dto.response.MyProjectScrollResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MyProjectScrollResponseMapper {

    private final ImageService imageService;

    public MyProjectScrollResponseMapper(ImageService imageService) {
        this.imageService = imageService;
    }

    public MyProjectScrollResponse toResponse(
            List<MyProjectScrollResponse.ProjectPreview> rawContent,
            LocalDateTime nextCursorValue,
            Long nextProjectId
    ) {
        List<MyProjectScrollResponse.ProjectPreview> mappedContent = rawContent.stream()
                .map(p -> new MyProjectScrollResponse.ProjectPreview(
                        p.projectId(),
                        p.title(),
                        p.isCompleted(),
                        p.createdAt(),
                        p.endAt(),
                        p.achievementRate(),
                        imageService.getFullUrl(p.imageUrl())
                ))
                .collect(Collectors.toList());

        return new MyProjectScrollResponse(mappedContent, nextCursorValue, nextProjectId);
    }
}
