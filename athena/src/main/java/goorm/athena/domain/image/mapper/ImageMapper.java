package goorm.athena.domain.image.mapper;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.imageGroup.entity.ImageGroup;

public class ImageMapper {
    // ImageCreatRequest(Dto) -> Image Entity
    public static Image toEntity(ImageCreateRequest request, ImageGroup imageGroup, Long imageIndex) {
        return Image.builder()
                .imageGroup(imageGroup)
                .fileName(request.fileName())
                .url(request.url())
                .fileType(request.fileType())
                .imageIndex(imageIndex)
                .build();
    }

}
