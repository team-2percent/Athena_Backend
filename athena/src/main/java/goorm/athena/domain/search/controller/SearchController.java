package goorm.athena.domain.search.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import lombok.RequiredArgsConstructor;
import goorm.athena.domain.search.service.SearchService;
import goorm.athena.domain.search.entity.Search;

@RequiredArgsConstructor
@RestController
public class SearchController {
  private final SearchService searchService;

  @GetMapping("/searchList")
  public Page<Search> searchList(
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "") String searchWord) {
    return this.searchService.getList(page, searchWord);
  }
}
