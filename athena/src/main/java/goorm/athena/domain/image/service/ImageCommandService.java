package goorm.athena.domain.image.service;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.mapper.ImageMapper;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;

import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Transactional
@RequiredArgsConstructor
@Service
public class ImageCommandService {
    private final ImageRepository imageRepository;

    private static final String IMAGE_EXTENSION = "webp";

    @Value("${image.server.url}")
    private String baseImageUrl;

    private final ImageQueryService imageQueryService;
    private final ImageMapper imageMapper;

    // 단일 이미지 업로드
    public String uploadImage(MultipartFile file, ImageGroup imageGroup) {
        return uploadImage(file, imageGroup, 0L);
    }

    public String uploadImage(MultipartFile file, ImageGroup imageGroup, Long imageIndex) {
        String originalFilename = file.getOriginalFilename();

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!List.of("jpg", "jpeg", "png", "webp").contains(fileExtension)) {
            ErrorCode errorCode = ErrorCode.INVALID_IMAGE_EXTENSION;
            throw new CustomException(errorCode, errorCode.getErrorMessage() + " : " + originalFilename);
        }

        String fileName = UUID.randomUUID().toString() + "." + IMAGE_EXTENSION;
        String fileUrl = baseImageUrl + "/" + fileName;

        try {
            WebClient webClient = WebClient.create();
            ResponseEntity<String> response = webClient.put()
                    .uri(fileUrl)
                    .bodyValue(file.getBytes())
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            if (response.getStatusCode() == HttpStatus.OK) {
                Image image = Image.builder()
                        .imageGroup(imageGroup)
                        .fileName(fileName)
                        .originalUrl(fileUrl)
                        .fileType("origin")
                        .imageIndex(imageIndex)
                        .build();
                imageRepository.save(image);
                return fileUrl;
            }
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.ORIGIN_IMAGE_UPLOAD_FAILED;
            throw new CustomException(errorCode, errorCode.getErrorMessage() + " : " + originalFilename, e);
        }
        return null;
    }

    // 다중 이미지 업로드
    // 이미지 인덱스는 항상 1부터 시작하며, 0은 마크다운을 나타낸다.
    public void uploadImages(List<MultipartFile> files, ImageGroup imageGroup) {
        Long imageIndex = 1L;
        for (MultipartFile file : files) {
            uploadImage(file, imageGroup, imageIndex++);
        }
    }

    // 마크다운 이미지 저장 및 주소 반환
    public List<String> uploadMarkdownImages(List<MultipartFile> files, ImageGroup imageGroup) {
        List<String> imageUrls = files.stream()
                .map(file -> uploadImage(file, imageGroup))
                .toList();

        return imageUrls;
    }

    // 이미지 단일 삭제
    public void deleteImage(Image image) {
        try {
            WebClient webClient = WebClient.create();
            ResponseEntity<String> response = webClient.delete()
                    .uri(image.getOriginalUrl())
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            if (response.getStatusCode() == HttpStatus.OK) {
                imageRepository.delete(image);
            }
        } catch (Exception e) {
            ErrorCode errorCode = ErrorCode.IMAGE_DELETE_FAILED;
            throw new CustomException(errorCode, errorCode.getErrorMessage() + " : " + image.getFileName(), e);
        }
    }

    // 이미지 전체 삭제
    public void deleteImages(ImageGroup imageGroup) {
        List<Image> images = imageRepository.findAllByImageGroup(imageGroup);
        images.forEach(this::deleteImage);
    }
}
