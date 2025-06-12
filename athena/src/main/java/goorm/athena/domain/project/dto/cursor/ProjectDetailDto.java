package goorm.athena.domain.project.dto.cursor;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;

import java.util.List;

public record ProjectDetailDto (
        Project project,
        Category category,
        User seller,
        List<Image> images
) { }
