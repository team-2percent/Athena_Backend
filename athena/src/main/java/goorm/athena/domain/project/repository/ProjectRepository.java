package goorm.athena.domain.project.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.entity.ApprovalStatus;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    @Query("SELECT p FROM Project p " +
            "JOIN FETCH p.seller " +
            "JOIN FETCH p.imageGroup " +
            "WHERE p.isApproved = 'APPROVED' " +
                        "ORDER BY p.views DESC LIMIT :limit")
        List<Project> findTopNWithImageGroupByOrderByViewsDesc(@Param("limit") int limit);


    @Query(value = """
                SELECT *
                FROM (
                    SELECT p.*,
                           ROW_NUMBER() OVER (PARTITION BY p.category_id ORDER BY p.views DESC, p.created_at ASC) AS rn
                    FROM project p
                    WHERE p.is_approved = 'APPROVED'
                ) ranked
                WHERE ranked.rn <= 5
            """, nativeQuery = true)
    List<Project> findTopViewedProjectsByCategory();

    // queryDSL로 최적화 예정
    @Query(value = """
            SELECT p.* 
            FROM project p
            JOIN platform_plan pp ON p.platform_plan_id = pp.id
            WHERE pp.name IN ('PRO', 'PREMIUM')
                AND p.is_approved = 'APPROVED'
            ORDER BY p.created_at DESC LIMIT 20
            """, nativeQuery = true)
    List<Project> findTop5ProjectsGroupedByPlatformPlan();

    Page<Project> findByIsApproved(ApprovalStatus isApproved, Pageable pageable);

    List<Project> findByEndAtIn(List<LocalDateTime> endDates);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Project p where p.id = :id")
    Optional<Project> findByIdWithLock(@Param("id") Long id);
}
