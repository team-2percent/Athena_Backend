package goorm.athena.domain.project.repository;

import goorm.athena.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    @Query("""
    SELECT p
    FROM Project p
    JOIN FETCH p.imageGroup ig
    ORDER BY p.views DESC
""")
    List<Project> findTop20WithImageGroupByOrderByViewsDesc();
}
