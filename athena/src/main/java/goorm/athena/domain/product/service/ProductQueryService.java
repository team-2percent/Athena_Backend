package goorm.athena.domain.product.service;

import goorm.athena.domain.option.entity.Option;
import goorm.athena.domain.option.repository.OptionRepository;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.mapper.ProductMapper;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;    // OptionService를 따로 만들지 않고 여기서 관리
    private final ProductMapper productMapper;

    // 상품 + 옵션 전체 리스트 조회
    public List<ProductResponse> getAllProducts(Project project) {
        return productRepository.findAllByProject(project).stream()
                .map(product -> {
                    List<String> options = getAllOptions(product);
                    return productMapper.toDetailDto(product, options);
                })
                .toList();
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

    // 상품과 연관된 옵션 리스트 전체 조회
    private List<String> getAllOptions(Product product) {
        return optionRepository.findAllByProduct(product).stream()
                .map(Option::getOptionName)
                .toList();
    }
}
