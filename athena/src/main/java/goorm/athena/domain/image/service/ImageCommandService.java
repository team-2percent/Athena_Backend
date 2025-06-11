package goorm.athena.domain.image.service;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.mapper.ImageMapper;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Transactional
@RequiredArgsConstructor
@Service
public class ImageCommandService {
    private final ImageRepository imageRepository;
    private final NasService nasService;
    private final ImageQueryService imageQueryService;

    // 다중 이미지 업로드
    @Transactional
    public void uploadImages(List<MultipartFile> files, ImageGroup imageGroup) {
        Long imageGroupId = imageGroup.getId();
        List<ImageCreateRequest> requests = nasService.saveAll(files, imageGroupId); // NAS에 이미지 저장 및 DTO 반환

        List<Image> images = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            ImageCreateRequest request = requests.get(i);
            Image image = ImageMapper.toEntity(request, imageGroup, (long) (i + 1)); // 순서 부여
            images.add(image);
        }
        imageRepository.saveAll(images);
    }

    // 마크다운 이미지 저장 및 주소 반환
    @Transactional
    public List<String> uploadMarkdownImages(List<MultipartFile> files, ImageGroup imageGroup) {
        List<ImageCreateRequest> requests = nasService.saveAll(files, imageGroup.getId());

        List<Image> markdownImages = new ArrayList<>();
        for (ImageCreateRequest request : requests) {
            Image image = ImageMapper.toEntity(request, imageGroup, (long) 0);
            markdownImages.add(image);
        }
        imageRepository.saveAll(markdownImages);

        return imageQueryService.getImageUrls(markdownImages);
    }

    // 이미지 전체 삭제
    @Transactional
    public void deleteImages(ImageGroup imageGroup) {
        List<Image> images = imageRepository.findAllByImageGroup(imageGroup);
        for (Image image : images) {
            nasService.deleteImageFiles(image.getFileName()); // 이미지 삭제 (NAS)
        }
        imageRepository.deleteAll(images); // 이미지 삭제 (DB)
    }
}
