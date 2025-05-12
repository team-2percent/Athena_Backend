package goorm.athena.domain.product.service;


import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.mapper.ProductMapper;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    // 상품 리스트 저장
    public void createProducts(List<ProductRequest> requests, Project project){
        List<Product> products = requests.stream()
                .map(request -> ProductMapper.toEntity(request, project))
                .toList();

        productRepository.saveAll(products);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}
