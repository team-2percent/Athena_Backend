package goorm.athena.domain.comment.repository;

import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    boolean existsByUserAndProject(User user, Project project);
}
