package goorm.athena.domain.category.util;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import goorm.athena.domain.category.repository.CategoryRepository;

@Component
public class CategoryDataInitializer implements ApplicationRunner {
  private final CategoryRepository categoryRepository;

  public CategoryDataInitializer(CategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {
    List<String> defaultCategories = List.of(
        "디지털",
        "푸드",
        "패션",
        "생활",
        "예술",
        "출판",
        "게임",
        "기타");
    defaultCategories.forEach(categoryRepository::saveIfNotExist);
  }
}