package goorm.athena.domain.product.service;

import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.util.ProductIntegrationTestSupport;
import goorm.athena.domain.project.entity.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProductCommandServiceTest extends ProductIntegrationTestSupport {

    /***
     * 상품 테스트 시, 프로젝트(id=31)를 사용합니다.
     * 31번 프로젝트는 상품이 없으며, 상품 테스트용으로 생성한 프로젝트입니다.
     ***/

    @DisplayName("각 상품과 옵션들이 올바르게 매핑되어 정상적으로 저장된다." +
                "(프로젝트 31번 사용 중, 상품이 없는 프로젝트입니다.)")
    @Test
    void createProductAndOption(){
        // given
        Project project = projectRepository.findById(31L).orElseThrow();
        ProductRequest request1 = new ProductRequest("테스트 상품1", "설명1", 10000L, 100L, List.of("옵션1-1", "옵션1-2"));
        ProductRequest request2 = new ProductRequest("테스트 상품2", "설명2", 20000L, 200L, List.of("옵션2-1"));

        // when
        productCommandService.saveProducts(List.of(request1, request2), project);

        // then
        List<ProductResponse> products = productQueryService.getAllProducts(project);
        assertThat(products).hasSize(2);

        ProductResponse product1 = products.getFirst();
        assertThat(product1.name()).isEqualTo("테스트 상품1");
        assertThat(product1.description()).isEqualTo("설명1");
        assertThat(product1.price()).isEqualTo(10000L);
        assertThat(product1.stock()).isEqualTo(100L);
        assertThat(product1.options()).hasSize(2)
                .containsExactly("옵션1-1", "옵션1-2");

        ProductResponse product2 = products.get(1);
        assertThat(product2.name()).isEqualTo("테스트 상품2");
        assertThat(product2.description()).isEqualTo("설명2");
        assertThat(product2.price()).isEqualTo(20000L);
        assertThat(product2.stock()).isEqualTo(200L);
        assertThat(product2.options()).hasSize(1)
                .containsExactly("옵션2-1");
    }

    @DisplayName("각 상품과 옵션들이 올바르게 매핑되어 정상적으로 저장된다. 단, 옵션이 비어있는 경우가 존재한다." +
                "(프로젝트 31번 사용 중, 상품이 없는 프로젝트입니다.)")
    @Test
    void createProductAndEmptyOption(){
        // given
        Project project = projectRepository.findById(31L).orElseThrow();
        ProductRequest request1 = new ProductRequest("테스트 상품1", "설명1", 10000L, 100L, List.of("옵션1-1", "옵션1-2"));
        ProductRequest request2 = new ProductRequest("테스트 상품2", "설명2", 20000L, 200L, List.of());

        // when
        productCommandService.saveProducts(List.of(request1, request2), project);

        // then
        List<ProductResponse> products = productQueryService.getAllProducts(project);
        assertThat(products).hasSize(2);

        ProductResponse product1 = products.getFirst();
        assertThat(product1.name()).isEqualTo("테스트 상품1");
        assertThat(product1.description()).isEqualTo("설명1");
        assertThat(product1.price()).isEqualTo(10000L);
        assertThat(product1.stock()).isEqualTo(100L);
        assertThat(product1.options()).hasSize(2)
                .containsExactly("옵션1-1", "옵션1-2");

        ProductResponse product2 = products.get(1);
        assertThat(product2.name()).isEqualTo("테스트 상품2");
        assertThat(product2.description()).isEqualTo("설명2");
        assertThat(product2.price()).isEqualTo(20000L);
        assertThat(product2.stock()).isEqualTo(200L);
        assertThat(product2.options()).isEmpty();
    }

    @DisplayName("상품에 대한 정보를 업데이트 하는 경우, 재고만 변경된다." +
                "(프로젝트 31번 사용 중, 상품이 없는 프로젝트입니다.)")
    @Test
    void updateProduct(){
        // given
        Project project = projectRepository.findById(31L).orElseThrow();
        ProductRequest request1 = new ProductRequest("상품1", "설명1", 10000L, 100L, List.of("옵션1-1", "옵션1-2"));
        ProductRequest request2 = new ProductRequest("상품2", "설명2", 20000L, 200L, List.of());
        productCommandService.saveProducts(List.of(request1, request2), project);

        ProductRequest updateRequest1 = new ProductRequest(null, null, null, 50L, null);
        ProductRequest updateRequest2 = new ProductRequest(null, null, null, 150L, null);

        // when
        productCommandService.updateProducts(List.of(updateRequest1, updateRequest2), project);

        // then
        List<ProductResponse> products = productQueryService.getAllProducts(project);
        assertThat(products).hasSize(2);
        assertThat(products.get(0).stock()).isEqualTo(50L);
        assertThat(products.get(1).stock()).isEqualTo(150L);
    }

    @DisplayName("상품과 상품과 연결된 옵션들이 모두 삭제된다." +
                "(프로젝트 31번 사용 중, 상품이 없는 프로젝트입니다.)")
    @Test
    void deleteProductAndOption(){
        // given
        Project project = projectRepository.findById(31L).orElseThrow();
        ProductRequest request1 = new ProductRequest("상품1", "설명1", 10000L, 100L, List.of("옵션1-1", "옵션1-2"));
        ProductRequest request2 = new ProductRequest("상품2", "설명2", 20000L, 200L, List.of("옵션2-1"));
        productCommandService.saveProducts(List.of(request1, request2), project);

        // when
        productCommandService.deleteAllByProject(project);

        // then
        List<ProductResponse> products = productQueryService.getAllProducts(project);
        assertThat(products).isEmpty();
    }

}