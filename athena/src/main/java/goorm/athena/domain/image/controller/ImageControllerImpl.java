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

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ImageControllerImpl implements ImageController {

    private final ImageService imageService;
    private final ImageGroupService imageGroupService;

    // 프로젝트 이미지 업로드
    @Override
    public ResponseEntity<List<ImageCreateResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("imageGroupId") Long imageGroupId
    ) {
        ImageGroup imageGroup = imageGroupService.getById(imageGroupId);
        imageService.uploadImages(files, imageGroup);                                   // 이미지 S3 + DB 업로드
        List<ImageCreateResponse> responses = imageService.createResponses(imageGroup); // 응답 DTO 생성
        return ResponseEntity.ok(responses);
    }
}
