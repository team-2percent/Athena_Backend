package goorm.athena.domain.product.entity;

import goorm.athena.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;

    private String name;                // 상품명
    private String description;         // 상품 설명
    private Long price;                 // 상품 가격
    private Long stock;                 // 상품 재고

    @Builder
    private Product(Project project, String name, String description, Long price, Long stock){
        this.project = project;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public String getProductName() {
        return name;
    }

    public Long getProductPrice() {
        return price;
    }

}
