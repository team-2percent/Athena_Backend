package goorm.athena.domain.imageGroup.service;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.repository.ImageGroupRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ImageGroupQueryService {

    private final ImageGroupRepository imageGroupRepository;

    // Get Image group
    public ImageGroup getById(Long id) {
        return imageGroupRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_GROUP_NOT_FOUND));
    }
}
