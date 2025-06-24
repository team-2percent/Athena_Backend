package goorm.athena.domain.product.repository;

import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.Project;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
  
    List<Product> findByProjectId(Long projectId);

    List<Product> findAllByProject(Project project);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

}