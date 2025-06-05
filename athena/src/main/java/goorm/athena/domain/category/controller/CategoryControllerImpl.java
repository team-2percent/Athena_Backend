package goorm.athena.domain.category.controller;

import goorm.athena.domain.category.dto.res.CategoryAllGetResponse;
import goorm.athena.domain.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/category")
public class CategoryControllerImpl implements CategoryController{
    private final CategoryService categoryService;

    @Override
    @GetMapping
    public ResponseEntity<List<CategoryAllGetResponse>> getCategoryAll(){
        List<CategoryAllGetResponse> responses = categoryService.getCategories();
        return ResponseEntity.ok(responses);
    }
}
