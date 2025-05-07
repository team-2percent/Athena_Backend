package goorm.athena.domain.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import goorm.athena.domain.search.entity.Search;

// ToDo 아래 extends 부분은 추후 Project DTO 완성되면 변경 필요
public interface SearchRepository extends JpaRepository<Search, Long> {
  Search findByTitle(String title);

  Search findBySellerName(String sellerName);

  Page<Search> findAll(Specification<Search> spec, Pageable pageable);
}
