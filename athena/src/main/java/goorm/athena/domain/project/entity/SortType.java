package goorm.athena.domain.project.entity;

import io.swagger.v3.oas.annotations.media.Schema;

public enum SortType {
    DEADLINE,
    DEADLINE_RECOMMENDED,
    DEADLINE_POPULAR,
    DEADLINE_SUCCESS_RATE,
    LATEST, // 최신순
    RECOMMENDED, // 추천순
    POPULAR, // 인기순 (조회수)
    SUCCESS_RATE // 달성률순
}
