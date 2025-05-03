package goorm.athena.domain.bankaccount.entity;

import goorm.athena.domain.project.entity.Project;
import jakarta.persistence.*;

@Entity
public class BankAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String bankName;
    private String accountNumber;



}