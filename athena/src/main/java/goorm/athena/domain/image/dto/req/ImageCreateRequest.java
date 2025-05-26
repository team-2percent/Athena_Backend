package goorm.athena.domain.image.dto.req;

public record ImageCreateRequest (
    Long imageGroupId,
    String fileName,
    String url,
    String fileType
){  }
