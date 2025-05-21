package goorm.athena.domain.bankaccount.repository;


import goorm.athena.domain.bankaccount.entity.BankAccount;
import goorm.athena.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    // 판매자 기준으로 대표 계좌 1건 조회
    Optional<BankAccount> findByUserIdAndIsDefaultTrue(Long userId);

    List<BankAccount> findAllByUserId(Long userId);
}