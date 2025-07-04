package goorm.athena.domain.product.service;

import goorm.athena.domain.option.entity.Option;
import goorm.athena.domain.option.repository.OptionRepository;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.mapper.ProductMapper;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.entity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

@Transactional
@RequiredArgsConstructor
@Service
public class ProductCommandService {

    private final ProductRepository productRepository;
    private final ProductQueryService productQueryService;
    private final OptionRepository optionRepository;    // OptionService를 따로 만들지 않고 여기서 관리
    private final ProductMapper productMapper;

    // 상품 리스트 저장
    public void saveProducts(List<ProductRequest> requests, Project project) {
        List<Product> products = requests.stream()
                .map(req -> productMapper.toEntity(req, project))
                .toList();

        List<Product> saved = productRepository.saveAll(products);

        // 순서 보장 하에 옵션 생성
        IntStream.range(0, saved.size())
                .forEach(i -> {
                    ProductRequest req = requests.get(i);
                    if (req.options() != null && !req.options().isEmpty()) {
                        createOptions(saved.get(i), req);
                    }
                });
    }

    // 상품 업데이트 (수량만)
    public void updateProducts(List<ProductRequest> requests, Project project) {
        List<Product> products = productRepository.findAllByProject(project);

        IntStream.range(0, products.size())
                .forEach(i -> products.get(i).updateStock(requests.get(i).stock()));
    }

    // 상품 리스트 삭제
    public void deleteAllByProject(Project project) {
        List<Product> products = productRepository.findAllByProject(project);

        products.forEach(product -> {
            deleteOptions(product);
            productRepository.delete(product);
        });
    }

    // 옵션 생성
    private void createOptions(Product product, ProductRequest request) {
        List<Option> options = request.options().stream()
                .map(String::trim)
                .filter(name -> !name.isEmpty()) // 필터링만 하고
                .map(name -> new Option(product, name))
                .toList();

        if (options.isEmpty()) return;

        optionRepository.saveAll(options);
    }

    // 옵션 삭제
    private void deleteOptions(Product product) {
        List<Option> options = optionRepository.findAllByProduct(product);
        optionRepository.deleteAll(options);
    }


    public void updateStock(Long productId, Long stock) {
        Product product = productQueryService.getById(productId);
        product.updateStock(stock);
    }

}
