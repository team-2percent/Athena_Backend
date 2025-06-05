package goorm.athena.domain.project.util;

public enum ProjectQueryType {
  LATEST, // 신규순
  // POPULAR, // 인기순(조회수)
  CATEGORY, // 카테고리별
  DEADLINE, // 마감임박순
  // SUCCESS_RATE, // 달성률순
  SEARCH; // 검색
}
