package goorm.athena.domain.product.dto.res;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        Long price,
        Long stock,

        List<String> options
) {
}
