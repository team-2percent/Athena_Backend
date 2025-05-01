package goorm.athena.domain.search.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import goorm.athena.domain.search.service.SearchService;
import goorm.athena.domain.search.entity.Search;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
@RestController
public class SearchControllerImpl implements SearchController {
  private final SearchService searchService;

  @GetMapping("/searchList")
  public ResponseEntity<Page<Search>> searchList(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "") String searchWord) {
    Page<Search> result = this.searchService.getList(page, searchWord);
    return ResponseEntity.ok(result);
  }
}
