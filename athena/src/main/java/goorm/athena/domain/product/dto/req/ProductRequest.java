package goorm.athena.domain.product.dto.req;


import jakarta.persistence.Column;

import java.util.List;

public record ProductRequest (
        @Column(length = 25, nullable = false)
        String name,

        @Column(length = 50)
        String description,

        @Column(length = 100000000)
        Long price,

        @Column(length = 10000)
        Long stock,

        List<String> options
){ }
