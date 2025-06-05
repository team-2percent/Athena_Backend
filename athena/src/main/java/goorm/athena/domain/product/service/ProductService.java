package goorm.athena.domain.product.service;

import goorm.athena.domain.option.entity.Option;
import goorm.athena.domain.option.repository.OptionRepository;
import goorm.athena.domain.product.dto.req.ProductRequest;
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

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;    // OptionService를 따로 만들지 않고 여기서 관리

    // 상품 리스트 저장
    @Transactional
    public void saveProducts(List<ProductRequest> requests, Project project){
        List<Product> products = new ArrayList<>();

        for (ProductRequest request : requests) {
            Product product = ProductMapper.toEntity(request, project);
            products.add(product);
        }

        List<Product> savedProducts = productRepository.saveAll(products);  // 상품 일괄 저장

        // ProductRequest와 Product의 순서를 맞춰서 옵션 생성
        for (int i = 0; i < savedProducts.size(); i++) {
            ProductRequest request = requests.get(i);
            Product product = savedProducts.get(i);

            if (request.options() != null && !request.options().isEmpty()) {
                createOptions(product, request);
            }
        }
    }

    // 상품 리스트 삭제
    public void deleteAllByProject(Project project){
        List<Product> products = productRepository.findAllByProject(project);
        for (Product product : products){
            deleteOptions(product);
            productRepository.delete(product);
        }
    }

    public void updateProducts(List<ProductRequest> requests, Project project) {
        List<Product> products = productRepository.findAllByProject(project);
        List<Long> prices = requests.stream()
                .map(ProductRequest::price)
                .toList();

        for (int i = 0; i < products.size(); i++) {
            products.get(i).updatePrice(prices.get(i)); // 순서대로 가격 업데이트
        }
    }


    // 상품 리스트 전체 조회
    public List<ProductResponse> getAllProducts(Project project){
        List<Product> products = productRepository.findAllByProject(project);
        List<ProductResponse> productResponses = new ArrayList<>();

        for (Product product : products){
                List<String> options = getAllOptions(product);
                productResponses.add(ProductMapper.toDetailDto(product, options));
        }

        return productResponses;
    }

    // 옵션 리스트 생성
    private void createOptions(Product product, ProductRequest request) {
        List<Option> options = new ArrayList<>();

        for (String optionName : request.options()) {
            // 옵션이 빈 문자열이거나 NULL이 아니면 저장
            if (optionName != null && !optionName.isEmpty()) {
                Option option = new Option(product, optionName);
                options.add(option);
            } else {
                throw new CustomException(ErrorCode.OPTION_IS_EMPTY);
            }
        }

        optionRepository.saveAll(options);
    }

    // 옵션 리스트 삭제
    private void deleteOptions(Product product){
        List<Option> options = optionRepository.findAllByProduct(product);
        optionRepository.deleteAll(options);
    }

    /**
     * GET
     */

    // 상품 조회
    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    // 상품과 연관된 옵션 리스트 전체 조회
    private List<String> getAllOptions(Product product) {
        List<String> options = new ArrayList<>();
        List<Option> productOptions = optionRepository.findAllByProduct(product);
        productOptions.forEach(option -> options.add(option.getOptionName()));
        return options;
    }
    
    // 프로젝트 ID 기준으로 상품 리스트 조회
    public List<ProductResponse> getProductsByProjectId(Long projectId) {
        List<Product> products = productRepository.findByProjectId(projectId);
        return products.stream()
                .map(ProductResponse::from)
                .toList();
    }
}
