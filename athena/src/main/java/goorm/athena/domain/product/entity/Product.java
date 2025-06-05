package goorm.athena.domain.product.entity;

import goorm.athena.domain.project.entity.Project;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;
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

    @Column(length = 25)
    private String name;                // 상품명

    @Column(length = 50)
    private String description;         // 상품 설명

    @Max(1_000_000_000)
    private Long price;                 // 상품 가격

    @Max(50_000)
    private Long stock;                 // 상품 재고

    @Builder
    private Product(Project project, String name, String description, Long price, Long stock){
        this.project = project;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new CustomException(ErrorCode.INSUFFICIENT_INVENTORY);
        }
        this.stock -= quantity;
    }

    public String getProductName() {
        return name;
    }

    public Long getProductPrice() {
        return price;
    }

    public void updatePrice(Long price) {
        this.price = price;
    }

}
