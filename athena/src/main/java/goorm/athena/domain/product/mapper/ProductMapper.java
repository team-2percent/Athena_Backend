package goorm.athena.domain.product.mapper;

import goorm.athena.domain.product.dto.req.ProductRequest;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.entity.Product;
import goorm.athena.domain.project.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // ProductRequest -> Entity
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "project", source = "project")
    Product toEntity(ProductRequest request, Project project);

    // Entity -> ProductResponse (options 포함 X)
    @Mapping(target = "options", ignore = true)
    ProductResponse toDto(Product product);

    // Entity -> ProductResponse (options 포함)
    @Mapping(target = "options", source = "options")
    ProductResponse toDetailDto(Product product, List<String> options);

}
