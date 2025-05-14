package goorm.athena.domain.product.dto.req;

import goorm.athena.domain.option.entity.Option;

import java.util.List;

public record ProductRequest (
        String name,
        String description,
        Long price,
        Long stock,

        List<String> options
){ }
