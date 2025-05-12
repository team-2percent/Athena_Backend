package goorm.athena.domain.image.dto.req;

public record ImageCreateRequest (
    Long imageGroupId,
    String fileName,
    String originalUrl,
    String fileType
){
    // 이미지 그룹 ID를 받은 새로운 객체 return
    public ImageCreateRequest withImageGroupId(Long imageGroupId) {
        return new ImageCreateRequest(imageGroupId, fileName, originalUrl, fileType);
    }
}
