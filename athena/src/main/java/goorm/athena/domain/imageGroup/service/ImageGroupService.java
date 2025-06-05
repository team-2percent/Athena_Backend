package goorm.athena.domain.imageGroup.service;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.repository.ImageGroupRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ImageGroupService {
    private final ImageGroupRepository imageGroupRepository;

    // 이미지 그룹 생성 (엔터티)
    public ImageGroup createImageGroup(Type type) {
        ImageGroup imageGroup = new ImageGroup(type);
        return imageGroupRepository.save(imageGroup);
    }

    // 이미지 그룹 삭제
    public void deleteImageGroup(ImageGroup imageGroup) {
        imageGroupRepository.delete(imageGroup);
    }

    // Get Image group
    public ImageGroup getById(Long id) {
        return imageGroupRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_GROUP_NOT_FOUND));
    }
}
