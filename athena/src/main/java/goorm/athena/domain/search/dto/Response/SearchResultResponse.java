package goorm.athena.domain.search.dto.Response;

import java.util.List;

public record SearchResultResponse(
    List<Project> searchResults,
    long totalCount, // 전체 검색 결과 개수
    int pageSize, // 한 번에 담아서 보낼 개수
    int pageNumber,
    int totalPages) {
  public record Project(
      Long id,
      String sellerName, // 판매자 이름 (sellerId로 조인)
      // ToDo 추후 이미지 기능 구현됐을 때, 썸네일 이미지 조회 추가
      // String thumbnailUrl, // 썸네일 이미지 (imageGroupId로 조인)
      String title,
      String description,
      Long goalAmount,
      Long totalAmount,
      int daysLeft, // 남은 일자 (startAt, endAt으로 계산)
      String status) {
  }
}
