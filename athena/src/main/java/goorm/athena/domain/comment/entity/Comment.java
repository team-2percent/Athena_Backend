package goorm.athena.domain.comment.entity;

import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Getter
@RequiredArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(length = 1000)
    private String content;
    private LocalDateTime createdAt;

    @Builder
    public static Comment create(User user, Project project, String content){
        Comment comment = new Comment();
        comment.user = user;
        comment.project = project;
        comment.content = content;
        comment.createdAt = LocalDateTime.now();
        return comment;
    }
}
