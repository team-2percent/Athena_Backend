package goorm.athena.domain.comment.repository;

import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.project = :project")
    List<Comment> findByProjectWithUser(Project project);

    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.user u
        JOIN FETCH c.project p
        JOIN FETCH u.imageGroup ig
        WHERE p = :project
    """)
    List<Comment> findByProjectWithUserProfileImage(@Param("project") Project project);

    @Query("""
        SELECT c
        FROM Comment c
        JOIN FETCH c.project p
        JOIN FETCH p.imageGroup ig
        WHERE c.user = :user
    """)
    List<Comment> findByUserWithProjectImage(@Param("user") User user);

    boolean existsByUserAndProject(User user, Project project);
}
