package goorm.athena.domain.image.service;

import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.util.ImageIntegrationTestSupport;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ImageQueryServiceTest extends ImageIntegrationTestSupport {

    @DisplayName("썸네일 이미지를 조회는데 성공한다면, 해당 이미지의 URL을 반환한다.")
    @Test
    void getThumbnailImage(){
        // given
        ImageGroup imageGroup = setupImageGroup();
        MultipartFile file1 = createMockFile("test1.jpg", "image/jpeg");
        MultipartFile file2 = createMockFile("test2.jpg", "image/jpeg");

        imageCommandService.uploadImages(List.of(file1, file2), imageGroup);
        Image savedImage = imageQueryService.getAllImages(imageGroup).getFirst();

        // when
        String thumbnailUrl = imageQueryService.getImage(imageGroup.getId());

        // then
        assertThat(thumbnailUrl).isNotBlank();
        assertThat(thumbnailUrl).isEqualTo(savedImage.getOriginalUrl());
    }

    @DisplayName("썸네일 이미지를 조회하는데 실패한다면, 빈 문자열을 반환한다.")
    @Test
    void failToReadImages(){
        // given
        ImageGroup imageGroup = setupImageGroup();

        // when
        String thumbnailUrl = imageQueryService.getImage(imageGroup.getId());

        // then
        assertThat(thumbnailUrl).isEmpty();
    }

    @DisplayName("프로젝트 이미지를 조회하는 경우, 마크다운 이미지를 제외한 이미지들이 반환된다.")
    @Test
    void getProjectImages(){
        // given
        ImageGroup imageGroup = setupImageGroup();
        MultipartFile file1 = createMockFile("test1.jpg", "image/jpeg");
        MultipartFile file2 = createMockFile("test2.jpg", "image/jpeg");

        imageCommandService.uploadImages(List.of(file1), imageGroup);
        imageCommandService.uploadMarkdownImages(List.of(file2), imageGroup);

        List<Image> expectedProjectImages = imageQueryService.getAllImages(imageGroup).stream()
                .filter(image -> image.getImageIndex() != 0)
                .toList();

        // when
        List<Image> actualProjectImages = imageQueryService.getProjectImages(imageGroup.getId());

        // then
        assertThat(actualProjectImages).hasSize(1);
        assertThat(actualProjectImages)
                .usingRecursiveFieldByFieldElementComparator()
                .isEqualTo(expectedProjectImages);

    }

}