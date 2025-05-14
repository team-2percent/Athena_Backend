package goorm.athena.domain.image.controller;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.dto.res.ImageCreateResponse;
import goorm.athena.domain.image.service.ImageService;

import goorm.athena.domain.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class ImageControllerImpl implements ImageController {

    private final ImageService imageService;
    private final S3Service s3Service;

    // 프로젝트 이미지 업로드
    @Override
    public ResponseEntity<List<ImageCreateResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("imageGroupId") Long imageGroupId
    ) {
        // 파일을 S3 업로드
        List<ImageCreateRequest> imageCreateRequests = s3Service.uploadFiles(files);

        // 새 DTO 생성: imageGroupId 추가 ver
        List<ImageCreateRequest> updatedRequests = imageCreateRequests.stream()
                .map(req -> req.withImageGroupId(imageGroupId))
                .toList();

        // DTO를 사용하여 이미지 정보를 DB에 저장 후 응답 DTO return
        List<ImageCreateResponse> responses = imageService.uploadImages(updatedRequests);

        return ResponseEntity.ok(responses);
    }
}
