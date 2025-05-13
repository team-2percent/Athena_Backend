package goorm.athena.domain.settlement.entity;

import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bankaccount_id")
    private BankAccount bankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private int totalCount;
    private Long totalSales;
    private Long payOutAmount;
    private Long platformFee;

    private LocalDateTime settledAt;     // 정산 처리 시간

    @Enumerated(EnumType.STRING)
    private Status status;               // PENDING, COMPLETED, FAILED

    @Builder
    public Settlement(User user, BankAccount bankAccount, Project project,
                      int totalCount, Long totalSales, Long platformFee,
                      Long payOutAmount, Status status) {
        this.user = user;
        this.bankAccount = bankAccount;
        this.project = project;
        this.totalCount = totalCount;
        this.totalSales = totalSales;
        this.platformFee = platformFee;
        this.payOutAmount = payOutAmount;
        this.status = status;
        this.settledAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = Status.COMPLETED;
        this.settledAt = LocalDateTime.now();
    }
}