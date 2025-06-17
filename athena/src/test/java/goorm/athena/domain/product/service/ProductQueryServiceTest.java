package goorm.athena.domain.product.service;

import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.util.ProductIntegrationTestSupport;
import goorm.athena.domain.project.entity.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProductQueryServiceTest extends ProductIntegrationTestSupport {

    @DisplayName("전체 리스트를 조회하는 경우, 상품과 상품과 연관된 옵션들이 모두 올바르게 매핑되어 조회된다." +
                "(프로젝트 30번 사용 중, 옵션을 추가한 프로젝트입니다.)")
    @Test
    void getAllProductsAndOptions(){
        // given
        Project project = projectRepository.findById(30L).orElseThrow();

        // when
        List<ProductResponse> products = productQueryService.getAllProducts(project);

        // then
        assertThat(products).hasSize(5);

        ProductResponse product1 = products.getFirst();
        assertThat(product1.name()).isEqualTo("상품120");
        assertThat(product1.description()).isEqualTo("상품120 설명입니다.");
        assertThat(product1.price()).isEqualTo(85000L);
        assertThat(product1.stock()).isEqualTo(100L);
        assertThat(product1.options()).hasSize(2)
                .containsExactly("옵션1-1", "옵션1-2");

        ProductResponse product2 = products.get(1);
        assertThat(product2.name()).isEqualTo("상품121");
        assertThat(product2.description()).isEqualTo("상품121 설명입니다.");
        assertThat(product2.price()).isEqualTo(28000L);
        assertThat(product2.stock()).isEqualTo(100L);
        assertThat(product2.options()).hasSize(1)
                .containsExactly("옵션2-1");

        ProductResponse product3 = products.get(2);
        assertThat(product3.name()).isEqualTo("상품122");
        assertThat(product3.description()).isEqualTo("상품122 설명입니다.");
        assertThat(product3.price()).isEqualTo(44000L);
        assertThat(product3.stock()).isEqualTo(100L);
        assertThat(product3.options()).hasSize(1)
                .containsExactly("옵션3-1");

        ProductResponse product4 = products.get(3);
        assertThat(product4.name()).isEqualTo("상품123");
        assertThat(product4.description()).isEqualTo("상품123 설명입니다.");
        assertThat(product4.price()).isEqualTo(41000L);
        assertThat(product4.stock()).isEqualTo(100L);
        assertThat(product4.options()).hasSize(2)
                .containsExactly("옵션4-1", "옵션4-2");

        ProductResponse product5 = products.get(4);
        assertThat(product5.name()).isEqualTo("상품124");
        assertThat(product5.description()).isEqualTo("상품124 설명입니다.");
        assertThat(product5.price()).isEqualTo(59000L);
        assertThat(product5.stock()).isEqualTo(100L);
        assertThat(product5.options()).isEmpty();
    }

    @DisplayName("특정 프로젝트 ID를 기준으로 상품만 조회한다." +
                "(프로젝트 30번 사용 중, 옵션을 추가한 프로젝트입니다.)")
    @Test
    void getProductsByProjectId(){
        // given
        Long projectId = 30L;

        // when
        List<ProductResponse> products = productQueryService.getProductsByProjectId(projectId);

        // then
        assertThat(products).hasSize(5);

        ProductResponse product1 = products.getFirst();
        assertThat(product1.name()).isEqualTo("상품120");
        assertThat(product1.description()).isEqualTo("상품120 설명입니다.");
        assertThat(product1.price()).isEqualTo(85000L);
        assertThat(product1.stock()).isEqualTo(100L);

        ProductResponse product2 = products.get(1);
        assertThat(product2.name()).isEqualTo("상품121");
        assertThat(product2.description()).isEqualTo("상품121 설명입니다.");
        assertThat(product2.price()).isEqualTo(28000L);
        assertThat(product2.stock()).isEqualTo(100L);

        ProductResponse product3 = products.get(2);
        assertThat(product3.name()).isEqualTo("상품122");
        assertThat(product3.description()).isEqualTo("상품122 설명입니다.");
        assertThat(product3.price()).isEqualTo(44000L);
        assertThat(product3.stock()).isEqualTo(100L);

        ProductResponse product4 = products.get(3);
        assertThat(product4.name()).isEqualTo("상품123");
        assertThat(product4.description()).isEqualTo("상품123 설명입니다.");
        assertThat(product4.price()).isEqualTo(41000L);
        assertThat(product4.stock()).isEqualTo(100L);

        ProductResponse product5 = products.get(4);
        assertThat(product5.name()).isEqualTo("상품124");
        assertThat(product5.description()).isEqualTo("상품124 설명입니다.");
        assertThat(product5.price()).isEqualTo(59000L);
        assertThat(product5.stock()).isEqualTo(100L);

    }
}