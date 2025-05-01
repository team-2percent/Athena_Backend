package goorm.athena.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {


    // 결제
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 주문이 존재하지 않습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 결제가 존재하지 않습니다."),
    KAKAO_PAY_REQUEST_FAILED(HttpStatus.NOT_FOUND, "카카오페이 결제 요청 실패"),
    KAKAO_PAY_APPROVE_FAILED(HttpStatus.NOT_FOUND, "카카오페이 승인 실패"),
    JSON_PROCESSING_ERROR(HttpStatus.NOT_FOUND, "JSON 직렬화 실패: 결제 요청 파라미터를 JSON으로 변환할 수 없습니다");
//    PAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 Episode의 Page가 존재하지 않습니다."),
//    EPISODE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 Episode가 존재하지 않습니다.");

    private final HttpStatus errorCode;
    private final String errorMessage;

    ErrorCode(HttpStatus errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
