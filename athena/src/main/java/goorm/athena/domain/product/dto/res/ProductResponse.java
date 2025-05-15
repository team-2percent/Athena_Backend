package goorm.athena.domain.product.dto.res;

import goorm.athena.domain.product.entity.Product;
import lombok.Builder;

@Builder
public record ProductResponse(
        Long id,
        String name,
        String description,
        Long price,
        Long stock
) {
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .build();
    }
}