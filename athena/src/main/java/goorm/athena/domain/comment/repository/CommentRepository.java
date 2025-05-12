package goorm.athena.domain.comment.repository;

import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByProject(Project project);
}
