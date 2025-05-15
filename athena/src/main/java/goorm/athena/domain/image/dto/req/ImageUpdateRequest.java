package goorm.athena.domain.image.dto.req;

import org.springframework.web.multipart.MultipartFile;

public record ImageUpdateRequest(
        String url,
        MultipartFile file
) { }
