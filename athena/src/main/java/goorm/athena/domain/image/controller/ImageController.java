package goorm.athena.domain.image.controller;

import goorm.athena.domain.image.dto.res.ImageCreateResponse;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Image", description = "Image API")
@RequestMapping("/api")
public interface ImageController {

    @Operation(summary = "프로젝트 이미지 업로드 API", description = "프로젝트 이미지를 업로드합니다.<br>")
    @ApiResponse(responseCode = "200", description = "프로젝트 이미지 업로드 성공",
            content = @Content(schema = @Schema(implementation = ProjectIdResponse.class)))
    @PostMapping("/images")
    ResponseEntity<List<ImageCreateResponse>> uploadImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("imageGroupId") Long imageGroupId
    );
}
