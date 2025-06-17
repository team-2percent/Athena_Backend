package goorm.athena.domain.image.service;

import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ImageQueryService {
    @Value("${image.server.url}")
    private String baseImageUrl;

    private final ImageRepository imageRepository;

    // 썸네일 이미지 불러오기
    public String getImage(Long imageGroupId) {
        return imageRepository.findFirstImageByImageGroupId(imageGroupId)
                .map(Image::getOriginalUrl)
                .orElse("");
    }

    // 프로젝트 이미지 불러오기
    // (마크다운 이미지는 제외한다.)
    public List<Image> getProjectImages(Long imageGroupId) {
        return imageRepository.findProjectImagesByImageGroupId(imageGroupId);
    }

    // 이미지 url 리스트 불러오기
    public List<String> getImageUrls(List<Image> images) {
        return images.stream()
                .map(Image::getOriginalUrl)
                .toList();
    }

    // 모든 이미지 불러오기
    public List<Image> getAllImages(ImageGroup imageGroup) {
        return imageRepository.findAllByImageGroup(imageGroup);
    }

    /*
     * Path로 이미지 Full URL 조립
     */
    public String getFullUrl(String path) {
        return baseImageUrl + "/" + path;
    }
}
