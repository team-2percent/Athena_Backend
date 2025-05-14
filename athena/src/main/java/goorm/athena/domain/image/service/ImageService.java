package goorm.athena.domain.image.service;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.dto.res.ImageCreateResponse;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.mapper.ImageMapper;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageGroupService imageGroupService;
    private final S3Service s3Service;

    // 복수 이미지 저장
    public List<ImageCreateResponse> uploadImages(List<ImageCreateRequest> requests) {
        List<Image> images = new ArrayList<>();

        for (ImageCreateRequest request : requests) {
            ImageGroup imageGroup = imageGroupService.getById(request.imageGroupId());
            Image image = ImageMapper.toEntity(request, imageGroup);
            images.add(image);
        }
        List<Image> savedImages = imageRepository.saveAll(images);   // 이미지 Entity 일괄 저장

        List<ImageCreateResponse> responses = new ArrayList<>();
        for (Image saved : savedImages) {
            responses.add(ImageMapper.toCreateDto(saved));
        }

        return responses;
    }

    /*
        [변경 사항이 있는 이미지만 수정]
        existingUrls: 기존에 존재하는 이미지 중 남아 있는 이미지의 URL
        newImageFiles: 새로 들어온 이미지 파일들
    */
    @Transactional
    public void updateImages(ImageGroup imageGroup,
                             List<String> existingUrls, List<MultipartFile> newImageFiles) {
        List<Image> images = imageRepository.findAllByImageGroup(imageGroup);
        List<String> imageUrls = new ArrayList<>();
        for (Image image : images) {
            imageUrls.add(image.getOriginalUrl());
        }

        List<String> removeUrls = s3Service.compareImages(imageUrls, existingUrls); // Url 대조
        for (Image image : images) {
            if (removeUrls.contains(image.getOriginalUrl())) {
                imageRepository.delete(image);                                      // 제거할 URL에 해당되는 파일 제거 (DB)
            }
        }
        s3Service.deleteFiles(removeUrls);                                          // 제거할 URL에 해당되는 파일 제거 (S3)

        if (newImageFiles != null && newImageFiles.isEmpty()) {
            List<ImageCreateRequest> newImages = s3Service.uploadFiles(newImageFiles);  // 새로 들어온 파일 업로드 (S3)
            List<ImageCreateRequest> newImagesDto = newImages.stream()                  // 이미지 그룹 매핑한 DTO 생성
                    .map(req -> req.withImageGroupId(imageGroup.getId()))
                    .toList();

            uploadImages(newImagesDto);                                                 // 새로 들어온 파일 저장 (DB)
        }
    }

    // 연관 이미지 전체 삭제
    @Transactional
    public void deleteImages(ImageGroup imageGroup) {
        List<Image> images = imageRepository.findAllByImageGroup(imageGroup);
        List<String> fileUrls = new ArrayList<>();     // 고유한 파일 이름을 저장할 List
        for (Image image : images) {
            String fileUrl = image.getOriginalUrl();
            fileUrls.add(fileUrl);
        }

        s3Service.deleteFiles(fileUrls);                // S3에서 이미지 삭제
        imageRepository.deleteAll(images);              // DB에서 이미지 삭제
    }

    public String getImage(Long imageGroupId){
        return imageRepository.findFirstImageByImageGroupId(imageGroupId)
                .map(Image::getOriginalUrl)
                .orElse("");

    }
}
