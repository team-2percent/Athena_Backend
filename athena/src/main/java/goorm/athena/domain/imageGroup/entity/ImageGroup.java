package goorm.athena.domain.imageGroup.entity;

import goorm.athena.domain.project.entity.Status;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "image_group")
public class ImageGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Type type;
}
