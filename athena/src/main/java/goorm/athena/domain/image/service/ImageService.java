package goorm.athena.domain.image.service;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.dto.res.ImageCreateResponse;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.image.mapper.ImageMapper;
import goorm.athena.domain.image.repository.ImageRepository;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageGroupService imageGroupService;

    // 복수 이미지 저장
    public List<ImageCreateResponse> uploadImages(List<ImageCreateRequest> requests) {
        List<ImageCreateResponse> responses = new ArrayList<>();
        for (ImageCreateRequest request : requests) {
            ImageGroup imageGroup = imageGroupService.getById(request.imageGroupId());
            Image image = ImageMapper.toEntity(request, imageGroup);
            ImageCreateResponse response = ImageMapper.toCreateDto(imageRepository.save(image));

            responses.add(response);
        }

        return responses;
    }

    public String getImage(Long imageGroupId){
        return imageRepository.findFirstImageByImageGroupId(imageGroupId)
                .map(Image::getOriginalUrl)
                .orElse("");

    }
}
