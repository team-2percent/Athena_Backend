package goorm.athena.domain.image.entity;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.project.entity.Project;
import goorm.athena.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_group_id", nullable = false)
    private ImageGroup imageGroup;

    private String fileName;
    private String fileUrl;
    private String fileType;    // 파일형
}