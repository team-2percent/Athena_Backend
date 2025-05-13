package goorm.athena.domain.project.controller;

import goorm.athena.domain.imageGroup.entity.ImageGroup;
import goorm.athena.domain.imageGroup.entity.Type;
import goorm.athena.domain.imageGroup.service.ImageGroupService;
import goorm.athena.domain.product.dto.res.ProductResponse;
import goorm.athena.domain.product.service.ProductService;
import goorm.athena.domain.project.dto.req.ProjectApprovalRequest;
import goorm.athena.domain.project.dto.req.ProjectCreateRequest;
import goorm.athena.domain.project.dto.res.ProjectIdResponse;
import goorm.athena.domain.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects")
public class ProjectControllerImpl implements ProjectController {
    private final ProjectService projectService;
    private final ImageGroupService imageGroupService;
    private final ProductService productService;

    // 프로젝트 초기 설정 (이미지 그룹 생성)
    @Override
    public ResponseEntity<Long> initializeProject() {
        ImageGroup imageGroup = imageGroupService.createImageGroup(Type.PROJECT);
        return ResponseEntity.ok(imageGroup.getId());
    }

    // 프로젝트 생성
    @Override
    public ResponseEntity<ProjectIdResponse> createProject(@RequestBody ProjectCreateRequest request){
        ProjectIdResponse response = projectService.createProject(request); // 프로젝트 생성 로직
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{projectId}/products")
    public ResponseEntity<List<ProductResponse>> getProductsByProject(
            @PathVariable Long projectId
    ) {
        List<ProductResponse> productList = productService.getProductsByProjectId(projectId);
        return ResponseEntity.ok(productList);
    }

    @PatchMapping("/{projectId}/approval")
    public ResponseEntity<String> updateApprovalStatus(
            @PathVariable Long projectId,
            @RequestBody ProjectApprovalRequest request
    ) {
        projectService.updateApprovalStatus(projectId, request.approve());
        String resultMessage = request.approve() ? "승인되었습니다." : "거절되었습니다.";
        return ResponseEntity.ok(resultMessage);
    }
}
