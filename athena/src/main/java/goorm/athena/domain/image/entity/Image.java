package goorm.athena.domain.image.entity;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_group_id", nullable = false)
    private ImageGroup imageGroup;

    private String fileName;
    private String originalUrl;
    private String fileType;    // 파일형

    @Builder
    private Image(ImageGroup imageGroup, String fileName, String originalUrl, String fileType) {
        this.imageGroup = imageGroup;
        this.fileName = fileName;
        this.originalUrl = originalUrl;
        this.fileType = fileType;
    }
}