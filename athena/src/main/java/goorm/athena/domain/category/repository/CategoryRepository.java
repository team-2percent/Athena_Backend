package goorm.athena.domain.category.repository;

import java.util.Optional;

import goorm.athena.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<Category> findByCategoryName(String categoryName);

  default Category saveIfNotExist(String categoryName) {
    return findByCategoryName(categoryName)
        .orElseGet(() -> save(Category.builder()
            .categoryName(categoryName)
            .build()));
  }
}
