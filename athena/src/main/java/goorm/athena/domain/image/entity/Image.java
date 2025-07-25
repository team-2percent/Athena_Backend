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
    private String fileType;    // 파일 사이즈 표기로 변경

    private Long imageIndex;    // 이미지 순서 (Markdown = 0)

    @Builder
    private Image(ImageGroup imageGroup, String fileName, String originalUrl, String fileType, Long imageIndex) {
        this.imageGroup = imageGroup;
        this.fileName = fileName;
        this.originalUrl = originalUrl;
        this.fileType = fileType;
        this.imageIndex = imageIndex;
    }

}