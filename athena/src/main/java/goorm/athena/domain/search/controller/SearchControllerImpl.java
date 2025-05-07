package goorm.athena.domain.search.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import goorm.athena.domain.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.Size;
import goorm.athena.domain.search.dto.Response.SearchResultResponse;

@RequiredArgsConstructor
@RestController
// ToDo 아래 implements 부분은 추후 Project DTO 완성되면 변경 필요
public class SearchControllerImpl implements SearchController {
  private final SearchService searchService;

  @GetMapping("/searchList")
  public ResponseEntity<SearchResultResponse> searchList(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "") @Size(min = 1) String searchWord) {
    SearchResultResponse result = this.searchService.getList(page, searchWord);
    return ResponseEntity.ok(result);
  }
}
