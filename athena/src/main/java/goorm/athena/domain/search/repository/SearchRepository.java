package goorm.athena.domain.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import goorm.athena.domain.search.entity.Search;

public interface SearchRepository extends JpaRepository<Search, Long> {
  Search findByTitle(String title);

  Search findBySellerName(String sellerName);

  Page<Search> findAll(Specification<Search> spec, Pageable pageable);
}
