package goorm.athena.domain.image.service;

import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.util.ImageIntegrationTestSupport;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;


class ImageCommandServiceTest extends ImageIntegrationTestSupport {

    @DisplayName("프로젝트 대표 이미지 업로드가 성공하면, 자동 인덱싱 되어 정보들이 저장된다.")
    @Test
    void uploadImagesWithIndexing(){
        // given
        ImageGroup imageGroup = setupImageGroup();
        MultipartFile file1 = createMockFile("test1.jpg", "image/jpeg");
        MultipartFile file2 = createMockFile("test2.jpg", "image/jpeg");
        MultipartFile file3 = createMockFile("test3.jpg", "image/jpeg");

        // when
        imageCommandService.uploadImages(List.of(file1, file2, file3), imageGroup);

        // then
        List<Image> images = imageQueryService.getAllImages(imageGroup);

        assertThat(images).hasSize(3);
        assertThat(images.stream()
                    .map(Image::getImageIndex)
                    .sorted()
                    .collect(Collectors.toList()))
                .containsExactly(1L, 2L, 3L);
        assertThat(images).allSatisfy(image -> {
            assertThat(image.getImageGroup()).isEqualTo(imageGroup);
            assertThat(image.getFileName()).endsWith(".webp");
        });

    }

    @DisplayName("마크다운 첨부 이미지 업로드가 성공하면, 해당되는 이미지 URL을 반환한다." +
            "마크다운 이미지는 인덱싱이 되지 않고 index = 0으로 저장된다.")
    @Test
    void uploadMarkdownImages(){
        // given
        ImageGroup imageGroup = setupImageGroup();
        MultipartFile file1 = createMockFile("test1.jpg", "image/jpeg");
        MultipartFile file2 = createMockFile("test2.jpg", "image/jpeg");
        MultipartFile file3 = createMockFile("test3.jpg", "image/jpeg");

        // when
        List<String> imageUrls = imageCommandService.uploadMarkdownImages(List.of(file1, file2, file3), imageGroup);

        // then
        assertThat(imageUrls).hasSize(3);
        assertThat(imageUrls).allSatisfy(url ->
                assertThat(url).endsWith(".webp")
        );

        List<Image> images = imageQueryService.getAllImages(imageGroup);
        assertThat(images).hasSize(3);
        assertThat(images).allSatisfy(image -> {
            assertThat(image.getImageIndex()).isEqualTo(0L);
            assertThat(imageUrls).contains(image.getOriginalUrl());
        });
    }

    @DisplayName("이미지 업로드 중 WebClient 상에서 에러가 발생하면, 예외가 발생한다.")
    @Test
    void uploadImageWithWebClientException(){
        // given
        String originalBaseImageUrl = (String) ReflectionTestUtils.getField(imageCommandService, "baseImageUrl");

        try {
            ReflectionTestUtils.setField(imageCommandService, "baseImageUrl", "http://invalid-url:9999");
            ImageGroup imageGroup = setupImageGroup();
            MultipartFile file = createMockFile("test.jpg", "image/jpeg");

            // when, then
            assertThatThrownBy(() -> imageCommandService.uploadImage(file, imageGroup))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ORIGIN_IMAGE_UPLOAD_FAILED);

        } finally {
            ReflectionTestUtils.setField(imageCommandService, "baseImageUrl", originalBaseImageUrl);    // 값 복원
        }
    }

    @DisplayName("이미지 확장자는 jpg, jpeg, png, webp만 허용한다.")
    @Test
    void uploadImageWithValidExtensions(){
        // given
        ImageGroup imageGroup = setupImageGroup();
        List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "webp");

        // when, then
        allowedExtensions.forEach(ext -> {
            MultipartFile file = createMockFile("test." + ext, "image/" + ext);
            assertDoesNotThrow(() -> {
                imageCommandService.uploadImages(List.of(file), imageGroup);
            });
        });

    }

    @DisplayName("이미지 확장자가 jpg, jpeg, png, webp가 아닌 다른 확장자라면 예외가 발생한다.")
    @Test
    void uploadImageWithInvalidExtensions(){
        // given
        ImageGroup imageGroup = setupImageGroup();
        List<String> disallowedExtensions = List.of("gif", "bmp", "svg", "exe");

        // when, then
        disallowedExtensions.forEach(ext -> {
            MultipartFile file = createMockFile("test." + ext, "image/" + ext);
            assertThatThrownBy(() -> imageCommandService.uploadImage(file, imageGroup))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_IMAGE_EXTENSION);
            }
        );
    }

    @DisplayName("정상적으로 서버에서 이미지가 삭제되면, 해당 이미지 그룹과 연결된 이미지가 전부 DB 에서 삭제된다.")
    @Test
    void deleteImages(){
        // given
        ImageGroup imageGroup = setupImageGroup();
        MultipartFile file1 = createMockFile("test1.jpg", "image/jpeg");
        MultipartFile file2 = createMockFile("test2.jpg", "image/jpeg");
        imageCommandService.uploadImages(List.of(file1, file2), imageGroup);

        // when
        imageCommandService.deleteImages(imageGroup);

        // then
        assertThat(imageQueryService.getAllImages(imageGroup)).isEmpty();
    }

//    @DisplayName("서버에서 이미지 삭제를 실패하면, 503 응답 코드를 받아온다.")
//    @Test
//    void failToDeleteImage(){
//        // given
//        ImageGroup imageGroup = setupImageGroup();
//        MultipartFile file1 = createMockFile("test1.jpg", "image/jpeg");
//        MultipartFile file2 = createMockFile("test2.jpg", "image/jpeg");
//        imageCommandService.uploadImages(List.of(file1, file2), imageGroup);
//
//        ReflectionTestUtils.setField(imageCommandService, "baseImageUrl", "http://invalid-url:9999");
//
//        // when, then
//        assertThatThrownBy(() -> imageCommandService.deleteImages(imageGroup))
//                .isInstanceOf(CustomException.class)
//                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IMAGE_DELETE_FAILED);
//        assertThat(imageQueryService.getAllImages(imageGroup)).hasSize(2);
//    }

}