package goorm.athena.domain.project.dto.req;

public record ProjectCursorRequest<T>(
        T cursorValue,  // startAt, endAt, views 등 정렬 기준
        Long cursorId,  // 동일한 cursorValue에 대한 고유 id 보조 커서
        int size
) {
    public static final int DEFAULT_SIZE = 20;

    public int getSize() {
        return size == 0 ? DEFAULT_SIZE : size;
    }
}