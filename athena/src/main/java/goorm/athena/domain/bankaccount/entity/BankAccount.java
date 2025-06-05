package goorm.athena.domain.bankaccount.entity;

import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_number", length = 50)
    private String accountNumber;

    @Column(name = "account_holder", length = 50)
    private String accountHolder;

    @Column(name = "bank_name", length = 50)
    private String bankName;

    private boolean isDefault;

    @Builder
    private BankAccount(User user, String accountNumber, String accountHolder, String bankName, boolean isDefault){
        this.user = user;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.bankName = bankName;
        this.isDefault = isDefault;
    }

    public void setAsDefault() { this.isDefault = true;}

    public void unsetAsDefault() { this.isDefault = false; }
}