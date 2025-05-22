package goorm.athena.domain.project.entity;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.category.entity.Category;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
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

    private String title;
    private String description;         // 설명 (요약)
    private Long goalAmount;
    private Long totalAmount;

    @Column(columnDefinition = "TEXT")
    private String contentMarkdown;     // 소개글 (마크 다운)

    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private LocalDateTime shippedAt;    // 발송 일자

    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ApprovalStatus isApprove = ApprovalStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private Status status = Status.QUEUED;


    private Long views = 0L;

    @Builder
    private Project(User seller, ImageGroup imageGroup, Category category, BankAccount bankAccount,
                    String title, String description,
                    Long goalAmount, Long totalAmount, String contentMarkdown,
                    LocalDateTime startAt, LocalDateTime endAt, LocalDateTime shippedAt, Status status,
                    PlatformPlan platformPlan) {
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
        this.status = status;
        this.platformPlan = platformPlan;
    }


    public void setApprovalStatus(boolean isApproved) {
        if (isApproved) {
            this.isApprove = ApprovalStatus.APPROVED;
            this.status = Status.ACTIVE;
        }
        else {
            this.isApprove = ApprovalStatus.REJECTED;
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

}