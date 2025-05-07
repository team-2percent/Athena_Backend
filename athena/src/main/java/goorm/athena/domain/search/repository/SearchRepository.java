package goorm.athena.domain.search.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import goorm.athena.domain.search.entity.Search;

public interface SearchRepository extends JpaRepository<Search, Long> {
}
