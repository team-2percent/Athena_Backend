package goorm.athena.domain.product.service;

import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.mapper.ProductMapper;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.product.repository.query.ProductQueryRepository;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository;
    private final ProductMapper productMapper;

    // 상품 + 옵션 전체 리스트 조회
    public List<ProductResponse> getAllProducts(Project project) {
        return productQueryRepository.getAllProducts(project);
    }

    // 단일 상품 조회
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 프로젝트 ID 기준으로 상품만 조회
    public List<ProductResponse> getProductsByProjectId(Long projectId) {
        List<Product> products = productRepository.findByProjectId(projectId);
        return products.stream()
                .map(productMapper::toDto)
                .toList();
    }

    public Product getProductWithLock(Long productId) {
        return productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

}
