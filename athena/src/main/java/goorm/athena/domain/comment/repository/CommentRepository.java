package goorm.athena.domain.comment.repository;

import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.project = :project")
    List<Comment> findByProjectWithUser(Project project);

    List<Comment> findByUser(User user);

    boolean existsByUserAndProject(User user, Project project);
}
