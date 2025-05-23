package goorm.athena.domain.image.controller;

import goorm.athena.domain.image.dto.res.ImageCreateResponse;
import goorm.athena.domain.image.service.ImageService;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@RestController
public class ImageControllerImpl implements ImageController {

    private final ImageService imageService;
    private final ImageGroupService imageGroupService;

    // 프로젝트 이미지 업로드
    @Override
    public ResponseEntity<Void> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("imageGroupId") Long imageGroupId
    ) throws IOException {
        imageService.uploadImages(files, imageGroupId);
        return ResponseEntity.ok().build();
    }
}
