package goorm.athena.domain.product.entity;

import goorm.athena.domain.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Project project;

    private String productName;         // 상품명
    private String productDescription;  // 상품 설명
    private Long productPrice;          // 상품 가격
    private Long stock;                 // 상품 재고
}
