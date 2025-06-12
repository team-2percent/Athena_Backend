package goorm.athena.domain.image.mapper;

import goorm.athena.domain.image.dto.req.ImageCreateRequest;
import goorm.athena.domain.image.entity.Image;
import goorm.athena.domain.imageGroup.entity.ImageGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    @Mapping(target = "imageGroup", source = "imageGroup")
    @Mapping(target = "imageIndex", source = "imageIndex")
    Image toEntity(ImageCreateRequest request, ImageGroup imageGroup, Long imageIndex);
}
