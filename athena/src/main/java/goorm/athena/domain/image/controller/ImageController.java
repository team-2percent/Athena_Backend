package goorm.athena.domain.image.controller;

import goorm.athena.domain.image.dto.res.ImageCreateResponse;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Image", description = "Image API")
@RequestMapping("/api/images")
public interface ImageController {

    @Operation(summary = "프로젝트 이미지 업로드 API", description = "프로젝트 이미지를 업로드합니다.<br>"
            + "MultipartFile을 이용해 여러 이미지를 한 번에 등록할 수 있습니다.")
    @ApiResponse(responseCode = "200", description = "프로젝트 이미지 업로드 성공",
            content = @Content(schema = @Schema(implementation = ProjectIdResponse.class)))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<List<ImageCreateResponse>> uploadImages(
            @Parameter(description = "업로드할 이미지 파일들", required = true)
            @RequestParam("files") List<MultipartFile> files,

            @Parameter(description = "이미지 그룹 ID", required = true)
            @RequestParam("imageGroupId") Long imageGroupId
    );
}
