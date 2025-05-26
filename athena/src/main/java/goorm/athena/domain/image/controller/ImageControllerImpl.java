package goorm.athena.domain.image.controller;


import goorm.athena.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ImageControllerImpl implements ImageController {

    private final ImageService imageService;

    // 프로젝트 이미지 업로드
    @Override
    public ResponseEntity<Void> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("imageGroupId") Long imageGroupId
    ) {
        imageService.uploadImages(files, imageGroupId);
        return ResponseEntity.ok().build();
    }
}
