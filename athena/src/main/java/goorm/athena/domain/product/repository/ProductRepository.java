package goorm.athena.domain.product.repository;

import goorm.athena.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProjectId(Long projectId);
}