package goorm.athena.domain.image.service;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.mapper.ImageMapper;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageGroupService imageGroupService;
    private final NasService nasService;

    /**
     * [이미지 저장]
     */
    // 다중 이미지 업로드
    @Transactional
    public void uploadImages(List<MultipartFile> files, Long imageGroupId) {
        List<ImageCreateRequest> requests;
        try {
            requests = nasService.saveAll(files, imageGroupId); // NAS에 이미지 저장 및 DTO 반환
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
        ImageGroup imageGroup = imageGroupService.getById(imageGroupId);

        List<Image> images = new ArrayList<>();
        for(int i = 0; i < requests.size(); i++) {
            ImageCreateRequest request = requests.get(i);
            Image image = ImageMapper.toEntity(request, imageGroup, (long) (i + 1));    // 순서 부여
            images.add(image);
        }
        imageRepository.saveAll(images);
    }

    // 마크다운 이미지 저장 및 주소 반환
    @Transactional
    public List<String> uploadMarkdownImages(List<MultipartFile> files, ImageGroup imageGroup) {
        List<ImageCreateRequest> requests;
        try {
            requests = nasService.saveAll(files, imageGroup.getId());
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        List<Image> markdownImages = new ArrayList<>();
        for (ImageCreateRequest request : requests) {
            Image image = ImageMapper.toEntity(request, imageGroup, null);
            markdownImages.add(image);
        }
        imageRepository.saveAll(markdownImages);

        return getImageUrls(markdownImages);
    }

    // 이미지 전체 삭제
    @Transactional
    public void deleteImages(ImageGroup imageGroup) {
        List<Image> images = imageRepository.findAllByImageGroup(imageGroup);
        for (Image image : images) {
            nasService.deleteImageFiles(image.getFileName());   // 이미지 삭제 (NAS)
        }
        imageRepository.deleteAll(images);                      // 이미지 삭제 (DB)
    }

    /**
     * [GET method]
     */
    // 썸네일 이미지 불러오기
    public String getImage(Long imageGroupId){
        return imageRepository.findFirstImageByImageGroupId(imageGroupId)
                .map(Image::getOriginalUrl)
                .orElse("");

    }

    // 프로젝트 이미지 불러오기
    // (마크다운 이미지는 제외한다.)
    public List<Image> getProjectImages(Long imageGroupId){
        return imageRepository.findProjectImagesByImageGroupId(imageGroupId);
    }

    // 이미지 url 리스트 불러오기
    public List<String> getImageUrls(List<Image> images) {
        List<String> imageUrls = new ArrayList<>();
        for (Image image : images) {
            imageUrls.add(image.getOriginalUrl());
        }
        return imageUrls;
    }

}
