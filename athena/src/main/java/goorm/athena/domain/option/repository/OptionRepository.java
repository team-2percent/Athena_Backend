package goorm.athena.domain.option.repository;

import goorm.athena.domain.option.entity.Option;
import goorm.athena.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Long>{
    List<Option> findAllByProduct(Product product);
}
