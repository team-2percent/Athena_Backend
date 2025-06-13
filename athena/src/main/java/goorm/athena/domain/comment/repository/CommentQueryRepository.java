package goorm.athena.domain.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import goorm.athena.domain.comment.entity.Comment;
import goorm.athena.domain.comment.entity.QComment;
import goorm.athena.domain.imageGroup.entity.QImageGroup;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.project.entity.QProject;
import goorm.athena.domain.user.entity.QUser;
import goorm.athena.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepository {
    private final JPAQueryFactory jpaQueryFactory;

    public List<Comment> getCommentsByProject(Project projectParam){
        QComment comment = QComment.comment;
        QUser user = QUser.user;
        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;

        return jpaQueryFactory
                .selectFrom(comment)
                .join(comment.user, user).fetchJoin()
                .join(comment.project, project).fetchJoin()
                .join(user.imageGroup, imageGroup).fetchJoin()
                .where(comment.project.eq(projectParam))
                .fetch();
    }

    public List<Comment> getCommentsByUser(User userParam){
        QComment comment = QComment.comment;
        QProject project = QProject.project;
        QImageGroup imageGroup = QImageGroup.imageGroup;

        return jpaQueryFactory
                .selectFrom(comment)
                .join(comment.project, project).fetchJoin()
                .join(project.imageGroup, imageGroup).fetchJoin()
                .where(comment.user.eq(userParam))
                .fetch();
    }

}
