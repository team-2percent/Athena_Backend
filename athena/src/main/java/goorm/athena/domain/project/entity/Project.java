package goorm.athena.domain.project.entity;

import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;            // 판매자 ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_group_id", nullable = false)
    private ImageGroup imageGroup;  // 이미지 그룹 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;      // 카테고리 ID

    private String title;
    private String description;
    private Long goalAmount;
    private Long totalAmount;
    private String contentMarkdown;

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime shippedAt;

    @Enumerated(EnumType.STRING)
    private Status status;
}