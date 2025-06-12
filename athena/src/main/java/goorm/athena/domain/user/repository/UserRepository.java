package goorm.athena.domain.user.repository;

import goorm.athena.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByEmail(String email);

    User findByEmail(String email);

    @Query(value = """
            SELECT u.* 
            FROM project p
            JOIN `user` u ON p.seller_id = u.id
            WHERE p.id = :projectId
            """, nativeQuery = true)
    User findSellerByProjectId(@Param("projectId") Long projectId);
}

