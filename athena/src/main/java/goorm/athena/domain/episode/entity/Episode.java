package goorm.athena.domain.episode.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long episodeId;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int price;
    private Long viewCount;

}
