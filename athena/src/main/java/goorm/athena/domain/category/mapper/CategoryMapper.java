package goorm.athena.domain.category.mapper;

import goorm.athena.domain.category.dto.res.CategoryAllGetResponse;
import goorm.athena.domain.category.entity.Category;

public class CategoryMapper {
    public static CategoryAllGetResponse toGetResponse(Category category){
        return new CategoryAllGetResponse(
                category.getId(),
                category.getCategoryName()
        );
    }
}
