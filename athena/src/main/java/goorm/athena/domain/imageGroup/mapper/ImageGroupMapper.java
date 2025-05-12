package goorm.athena.domain.imageGroup.mapper;

import goorm.athena.domain.imageGroup.dto.req.ImageGroupRequest;
import goorm.athena.domain.imageGroup.dto.res.ImageGroupResponse;
import goorm.athena.domain.imageGroup.entity.ImageGroup;

// 1차 MVP 끝난 후 제거 예정
public class ImageGroupMapper {

    // ImageGroupRequest(Dto) -> Entity
    public static ImageGroup toEntity(ImageGroupRequest request) {
        return ImageGroup.builder()
                .type(request.type())
                .build();
    }

    // Entity -> ImageGroupResponse(Dto)
    public static ImageGroupResponse toCreateDto(ImageGroup imageGroup){
        return ImageGroupResponse.builder()
                .id(imageGroup.getId())
                .type(imageGroup.getType())
                .build();
    }
}
