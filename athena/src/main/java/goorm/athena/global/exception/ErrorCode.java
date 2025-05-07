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
    JSON_PROCESSING_ERROR(HttpStatus.NOT_FOUND, "JSON 직렬화 실패: 결제 요청 파라미터를 JSON으로 변환할 수 없습니다"),

    // 인증
    AUTH_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    AUTH_INVALID_LOGIN(HttpStatus.UNAUTHORIZED, "로그인 정보가 유효하지 않습니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    AUTH_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "필드 유효성 검증에 실패했습니다"),

    //유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 USER가 존재하지 않습니다."),


    // 주문
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 배송지가 존재하지 않습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품의 ID가 존재하지 않습니다."),

    // 프로젝트
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 프로젝트의 ID가 존재하지 않습니다."),

    // 카카오 api
    KAKAO_PAY_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "카카오페이 서버와의 통신에 실패했습니다. 잠시 후 다시 시도해주세요.");
    ALREADY_EXIST_USER(HttpStatus.BAD_REQUEST, "이미 존재하는 사용자입니다.");

    private final HttpStatus errorCode;
    private final String errorMessage;

    ErrorCode(HttpStatus errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
