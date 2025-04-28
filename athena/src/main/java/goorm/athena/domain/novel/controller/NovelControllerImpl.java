package goorm.athena.domain.novel.controller;


import goorm.athena.domain.novel.dto.req.NovelCreateRequest;
import goorm.athena.domain.novel.dto.res.NovelCreateResponse;
import goorm.athena.domain.novel.service.NovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/novels")
public class NovelControllerImpl implements NovelController {

    private final NovelService novelService;

    @Override
    @PostMapping
    public ResponseEntity<NovelCreateResponse> createNovel(@RequestBody NovelCreateRequest request) {
        NovelCreateResponse response = novelService.createNovel(request);
        return ResponseEntity.ok(response);
    }

}
