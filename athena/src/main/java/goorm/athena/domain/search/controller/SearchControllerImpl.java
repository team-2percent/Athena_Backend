package goorm.athena.domain.search.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import goorm.athena.domain.search.dto.response.SearchResultResponse;
import goorm.athena.domain.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

@RequiredArgsConstructor
@RestController
@Validated
public class SearchControllerImpl implements SearchController {
  private final SearchService searchService;

  @GetMapping("/api/search")
  public ResponseEntity<SearchResultResponse> searchList(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "") @Size(min = 1, message = "검색어는 최소 1글자 이상이어야 합니다.") String searchWord) {
    SearchResultResponse result = this.searchService.getList(page, searchWord);
    return ResponseEntity.ok(result);
  }
}
