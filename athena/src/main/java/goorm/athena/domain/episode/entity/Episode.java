package goorm.athena.domain.episode.entity;

import goorm.athena.domain.novel.entity.Novel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private int price;

    @Column(name = "view_count", nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long episodeNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id")
    private Novel novel;

    @Builder
    private Episode(Novel novel, String title, String content, int price, Long episodeNumber) {
        this.novel = novel;
        this.title = title;
        this.content = content;
        this.price = price;
        this.episodeNumber = episodeNumber;
    }

    public static Episode create(Novel novel, String title, String content, int price, Long episodeNumber){
        return new Episode(novel, title, content, price, episodeNumber);
    }

    public void update(String title, String content, int price){
        this.title = title;
        this.content = content;
        this.price = price;
    }
}
