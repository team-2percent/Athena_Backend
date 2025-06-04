package goorm.athena.domain.category.util;

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
    DefaultCategories.VALUES.forEach(categoryRepository::saveIfNotExist);
  }
}