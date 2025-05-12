package goorm.athena.domain.project.entity;

public enum SortType {
    DEADLINE,
    LATEST, // 최신순
    RECOMMENDED, // 추천순
    POPULAR, // 인기순 (조회수)
    SUCCESS_RATE // 달성률순
}
