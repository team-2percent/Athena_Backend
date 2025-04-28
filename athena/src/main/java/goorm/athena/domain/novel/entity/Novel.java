package goorm.athena.domain.novel.entity;

import goorm.athena.domain.novel.dto.req.NovelCreateRequest;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "novel")
public class Novel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Status status;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "like_count")
    private Long likeCount = 0L;

    @Column(name = "image_id")
    private Long imageId;

    @Builder
    public Novel(User author, String title, String summary, Status status, Long imageId) {
        this.author = author;
        this.title = title;
        this.summary = summary;
        this.status = status;
        this.imageId = imageId;
    }

    public static Novel create(User author, NovelCreateRequest request) {
        return Novel.builder()
                .author(author)
                .title(request.title())
                .summary(request.summary())
                .status(request.status())
                .imageId(request.imageId())
                .build();
    }
}