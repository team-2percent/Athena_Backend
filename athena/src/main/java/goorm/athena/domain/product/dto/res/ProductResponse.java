package goorm.athena.domain.product.dto.res;

import java.util.List;

import goorm.athena.domain.product.entity.Product;
import lombok.Builder;

@Builder
public record ProductResponse(
        Long id,
        String name,
        String description,
        Long price,
        Long stock,

        List<String> options
)  { }
