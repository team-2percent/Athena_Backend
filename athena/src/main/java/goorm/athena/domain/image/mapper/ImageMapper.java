package goorm.athena.domain.image.mapper;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.dto.res.ImageCreateResponse;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.imageGroup.entity.ImageGroup;

public class ImageMapper {
    // ImageCreatRequest(Dto) -> Image Entity
    public static Image toEntity(ImageCreateRequest request, ImageGroup imageGroup) {
        return Image.builder()
                .imageGroup(imageGroup)
                .fileName(request.fileName())
                .originalUrl(request.originalUrl())
                .fileType(request.fileType())
                .build();
    }

    // Entity -> ImageCreateResponse(Dto)
    public static ImageCreateResponse toCreateDto(Image image){
        return ImageCreateResponse.builder()
                .id(image.getId())
                .fileName(image.getFileName())
                .originalUrl(image.getOriginalUrl())
                .fileType(image.getFileType())
                .imageGroupId(image.getImageGroup().getId())
                .build();
    }
}
