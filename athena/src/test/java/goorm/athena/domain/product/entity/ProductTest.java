package goorm.athena.domain.product.entity;

import goorm.athena.domain.product.util.ProductIntegrationTestSupport;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest extends ProductIntegrationTestSupport {

    @DisplayName("받아온 값에 맞게 재고 정보가 수정된다.")
    @Test
    void updateProductPrice(){
        // given
        Project project = projectRepository.findById(1L).orElseThrow();
        Product product = setupProduct(project, "테스트 상품", "설명", 1000L, 100L);
        Long newStock = 200L;

        // when
        product.updateStock(newStock);

        // then
        assertThat(product.getStock()).isEqualTo(newStock);
    }

    @DisplayName("받아온 값에 맞게 재고가 감소한다.")
    @Test
    void updateProductStock(){
        // given
        Project project = projectRepository.findById(1L).orElseThrow();
        Product product = setupProduct(project, "테스트 상품", "설명", 1000L, 100L);
        int quantity = 10;

        // when
        product.decreaseStock(quantity);

        // then
        assertThat(product.getStock()).isEqualTo(90L);
    }

    @DisplayName("받아온 재고가 기존 재고 값보다 큰 경우, 예외가 발생한다.")
    @Test
    void updateProductStockGreaterThanOriginalStock(){
        // given
        Project project = projectRepository.findById(1L).orElseThrow();
        Product product = setupProduct(project, "테스트 상품", "설명", 1000L, 100L);
        int quantity = 200;

        // when, then
        assertThatThrownBy(() -> product.decreaseStock(quantity))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INSUFFICIENT_INVENTORY);
    }

    @DisplayName("getProductPrice()를 호출하면, 해당되는 상품 가격이 반환된다.")
    @Test
    void getProductPrice(){
        // given
        Project project = projectRepository.findById(1L).orElseThrow();
        Product product = setupProduct(project, "테스트 상품", "설명", 1000L, 100L);

        // when
        Long productPrice = product.getProductPrice();

        // then
        assertThat(productPrice).isEqualTo(1000L);
    }

    @DisplayName("getProductName()을 호출하면, 해당되는 상품 이름이 반환된다.")
    @Test
    void getProductName(){
        // given
        Project project = projectRepository.findById(1L).orElseThrow();
        Product product = setupProduct(project, "테스트 상품", "설명", 1000L, 100L);

        // when
        String productName = product.getProductName();

        // then
        assertThat(productName).isEqualTo("테스트 상품");
    }

}