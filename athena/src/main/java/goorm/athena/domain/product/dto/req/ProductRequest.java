package goorm.athena.domain.product.dto.req;

public record ProductRequest (
        String name,
        String description,
        Long price,
        Long stock
){ }
