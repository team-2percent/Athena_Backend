package goorm.athena.domain.project.repository;

import goorm.athena.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}