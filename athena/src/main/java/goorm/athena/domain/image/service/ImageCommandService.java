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

import java.util.List;
import java.util.stream.IntStream;

@Transactional
@RequiredArgsConstructor
@Service
public class ImageCommandService {
    private final ImageRepository imageRepository;
    private final NasService nasService;
    private final ImageQueryService imageQueryService;
    private final ImageMapper imageMapper;

    // 다중 이미지 업로드
    @Transactional
    public void uploadImages(List<MultipartFile> files, ImageGroup imageGroup) {
        Long imageGroupId = imageGroup.getId();
        List<ImageCreateRequest> requests = nasService.saveAll(files, imageGroupId); // NAS에 이미지 저장 및 DTO 반환

        List<Image> images =
                IntStream.range(0, requests.size())
                        .mapToObj(i -> imageMapper.toEntity(requests.get(i), imageGroup, (long) (i + 1)))
                        .toList();

        imageRepository.saveAll(images);
    }

    // 마크다운 이미지 저장 및 주소 반환
    @Transactional
    public List<String> uploadMarkdownImages(List<MultipartFile> files, ImageGroup imageGroup) {
        List<ImageCreateRequest> requests = nasService.saveAll(files, imageGroup.getId());

        List<Image> markdownImages = requests.stream()
                .map(request -> imageMapper.toEntity(request, imageGroup, 0L))
                .toList();

        imageRepository.saveAll(markdownImages);
        return imageQueryService.getImageUrls(markdownImages);
    }

    // 이미지 전체 삭제
    @Transactional
    public void deleteImages(ImageGroup imageGroup) {
        List<Image> images = imageRepository.findAllByImageGroup(imageGroup);
        images.forEach(image -> nasService.deleteImageFiles(image.getFileName()));
        imageRepository.deleteAll(images); // 이미지 삭제 (DB)
    }
}
