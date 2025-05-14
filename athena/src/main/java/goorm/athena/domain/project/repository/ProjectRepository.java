package goorm.athena.domain.project.repository;

import goorm.athena.domain.project.entity.ApprovalStatus;
import goorm.athena.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    List<Project> findAllByEndAtBefore(LocalDateTime date);
  
    @Query("""
    SELECT p
    FROM Project p
    JOIN FETCH p.imageGroup ig
    ORDER BY p.views DESC
    """)
    List<Project> findTop20WithImageGroupByOrderByViewsDesc();

    @Query("""
    SELECT DISTINCT p.order.project FROM Payment p
    WHERE p.order.project.endAt < :endAt
      AND p.order.project.status = 'COMPLETED'
      AND p.order.project.isApproved = 'APPROVED'
      AND p.order.project.totalAmount >= p.order.project.goalAmount
      AND p.order.isSettled = false
      AND p.status = 'APPROVED'
    """)
    List<Project> findProjectsWithUnsettledOrders(@Param("endAt") LocalDateTime endAt);

    Page<Project> findByIsApproved(ApprovalStatus isApproved, Pageable pageable);
}
