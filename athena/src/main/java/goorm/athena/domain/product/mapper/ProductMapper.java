package goorm.athena.domain.product.mapper;

import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.Project;

import java.util.List;

public class ProductMapper {
    // ProductRequest(Dto) -> Entity
    public static Product toEntity(ProductRequest request, Project project) {
        return Product.builder()
                .project(project)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .build();
    }

    // Create ProductResponse(Dto)
    public static ProductResponse toDetailDto(Product product, List<String> options) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                options
        );
    }
}
