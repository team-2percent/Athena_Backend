package goorm.athena.domain.product.service;


import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.dto.res.ImageCreateResponse;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.mapper.ImageMapper;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.mapper.ProductMapper;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.repository.ProjectRepository;
import goorm.athena.domain.project.service.ProjectService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    // 상품 리스트 저장
    public void createProducts(List<ProductRequest> requests, Project project){
        for (ProductRequest request : requests) {
            Product product = ProductMapper.toEntity(request, project);
            productRepository.save(product);
        }
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    public List<ProductResponse> getProductsByProjectId(Long projectId) {
        List<Product> products = productRepository.findByProjectId(projectId);
        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }
}
