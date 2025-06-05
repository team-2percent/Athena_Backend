package goorm.athena.domain.project.entity;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_plan_id", nullable = false)
    private PlatformPlan platformPlan;

    @NotBlank
    @Column(length = 50)
    private String title;

    // 설명 (요약)
    @NotBlank
    @Size(min = 10, max = 100)
    @Column(length = 100)
    private String description;

    @Max(1_000_000_000)
    private Long goalAmount;

    @Max(1_000_000_000)
    private Long totalAmount;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String contentMarkdown;     // 소개글 (마크 다운)

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime shippedAt;    // 발송 일자

    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ApprovalStatus isApproved = ApprovalStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private Status status = Status.QUEUED;


    private Long views = 0L;

    @Builder
    private Project(User seller, ImageGroup imageGroup, Category category, BankAccount bankAccount,
                    String title, String description,
                    Long goalAmount, Long totalAmount, String contentMarkdown,
                    LocalDateTime startAt, LocalDateTime endAt, LocalDateTime shippedAt,
            PlatformPlan platformPlan, Long views) {
        this.seller = seller;
        this.imageGroup = imageGroup;
        this.category = category;
        this.bankAccount = bankAccount;
        this.title = title;
        this.description = description;
        this.goalAmount = goalAmount;
        this.totalAmount = totalAmount;
        this.contentMarkdown = contentMarkdown;
        this.startAt = startAt;
        this.endAt = endAt;
        this.shippedAt = shippedAt;
        this.platformPlan = platformPlan;
        this.views = views;
    }


    public void setApprovalStatus(boolean isApproved) {
        if (isApproved) {
            this.isApproved = ApprovalStatus.APPROVED;
            this.status = Status.ACTIVE;
        }
        else {
            this.isApproved = ApprovalStatus.REJECTED;
            this.status = Status.CANCELLED;
        }
    }


    public void update(Category category, BankAccount bankAccount,
                       String title, String description, Long goalAmount, String contentMarkdown,
                       LocalDateTime startAt, LocalDateTime endAt, LocalDateTime shippedAt) {
        this.category = category;
        this.bankAccount = bankAccount;
        this.title = title;
        this.description = description;
        this.goalAmount = goalAmount;
        this.contentMarkdown = contentMarkdown;
        this.startAt = startAt;
        this.endAt = endAt;
        this.shippedAt = shippedAt;
    }
    
    // 조회수 증가
    public void increaseViews(){
        this.views++;
    }

    public void increasePrice(Long price){
        this.totalAmount += price;
    }


}