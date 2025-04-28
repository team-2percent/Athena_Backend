package goorm.athena.domain.image.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 이미지 ID

    @Column(nullable = false)
    private String fileUrl; // 이미지 파일 경로 또는 URL

    @Column(nullable = true)
    private String fileName; // 원본 파일명 (선택사항)

//    @Column(nullable = true)
//    private String extension; // 파일 확장자 (ex: jpg, png)

}