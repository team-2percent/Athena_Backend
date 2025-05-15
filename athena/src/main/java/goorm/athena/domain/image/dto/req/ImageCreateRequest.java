package goorm.athena.domain.image.dto.req;

import goorm.athena.domain.imageGroup.entity.ImageGroup;

public record ImageCreateRequest (
    ImageGroup imageGroup,
    String fileName,
    String originalUrl,
    String fileType
){
    // 이미지 그룹 ID를 받은 새로운 객체 return
    public ImageCreateRequest withImageGroupId(ImageGroup imageGroup) {
        return new ImageCreateRequest(imageGroup, fileName, originalUrl, fileType);
    }
}
