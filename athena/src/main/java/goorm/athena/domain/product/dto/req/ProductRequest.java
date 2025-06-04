package goorm.athena.domain.product.dto.req;


import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;


public record ProductRequest (
        @Column(length = 25, nullable = false)
        String name,

        @Column(length = 50)
        String description,

        @Column(length = 100000000)
        Long price,

        @NotNull
        @Size(min = 1, max = 10000)
        @Column(length = 10000)
        Long stock,

        List<String> options
){ }
