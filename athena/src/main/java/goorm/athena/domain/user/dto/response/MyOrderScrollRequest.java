package goorm.athena.domain.user.dto.response;

import java.time.LocalDateTime;

public record MyOrderScrollRequest(
        LocalDateTime nextCursorValue,
        Long nextOrderId,
        int pageSize
) {}