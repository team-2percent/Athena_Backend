package goorm.athena.domain.product.service;

import goorm.athena.domain.option.entity.Option;
import goorm.athena.domain.option.repository.OptionRepository;
import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.product.mapper.ProductMapper;
import goorm.athena.domain.product.repository.ProductRepository;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
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
                .forEach(i -> createOptions(saved.get(i), requests.get(i)));
    }

    // 상품 업데이트 (수량만)
    public void updateProducts(List<ProductRequest> requests, Project project) {
        List<Product> products = productRepository.findAllByProject(project);

        IntStream.range(0, products.size())
                .forEach(i -> products.get(i).updatePrice(requests.get(i).stock()));
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
        if (request.options() == null || request.options().isEmpty()) return;

        List<Option> options = request.options().stream()
                .map(String::trim)
                .peek(name -> {
                    if (name.isEmpty()) {
                        throw new CustomException(ErrorCode.OPTION_IS_EMPTY);
                    }
                })
                .map(name -> new Option(product, name))
                .toList();

        optionRepository.saveAll(options);
    }

    // 옵션 삭제
    private void deleteOptions(Product product) {
        List<Option> options = optionRepository.findAllByProduct(product);
        optionRepository.deleteAll(options);
    }

}
