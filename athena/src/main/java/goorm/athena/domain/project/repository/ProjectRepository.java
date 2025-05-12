package goorm.athena.domain.project.repository;

import goorm.athena.domain.project.dto.res.ProjectAllResponse;
import goorm.athena.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    Page<ProjectAllResponse> findByOrderByViewsDesc(Pageable pageable);

    Page<ProjectAllResponse> findByOrderByStartAtDesc(Pageable pageable);
}
