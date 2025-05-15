package goorm.athena.domain.category.service;

import goorm.athena.domain.category.dto.res.CategoryAllGetResponse;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.category.mapper.CategoryMapper;
import goorm.athena.domain.category.repository.CategoryRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryAllGetResponse> getCategories(){
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryMapper::toGetResponse)
                .toList();
    }

    // 카테고리 ID return
    public Category getCategoryById(Long id){
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
    }
}
