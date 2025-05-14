package goorm.athena.domain.project.dto.req;

public record ProjectApprovalRequest(
        boolean approve // true: 승인, false: 반려
) {}