package goorm.athena.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 프로젝트, 상품
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 프로젝트 ID가 존재하지 않습니다."),
    PRODUCT_IS_EMPTY(HttpStatus.NOT_FOUND, "등록된 상품이 없습니다."),
    OPTION_IS_EMPTY(HttpStatus.NOT_FOUND, "옵션은 빈 문자열일 수 없습니다."),
    INVALID_JSON_FORMAT(HttpStatus.NOT_FOUND, "JSON 형태가 올바르지 않습니다."),
    INVALID_PROJECT_ORDER(HttpStatus.BAD_REQUEST, "해당 카테고리에서는 할 수 없는 정렬입니다."),

    // 카테고리
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다."),

    // 이미지
    IMAGE_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이미지 그룹 ID가 존재하지 않습니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.NOT_FOUND, "이미지 업로드에 실패했습니다."),
    INVALID_IMAGE_EXTENSION(HttpStatus.NOT_FOUND, "이미지 확장자가 올바르지 않습니다."),

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
    AUTH_MALFORMED_TOKEN(HttpStatus.BAD_REQUEST, "토큰의 형식이 잘못되었습니다"),
    AUTH_INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "서명이 잘못된 토큰입니다."),
    AUTH_EMPTY_TOKEN(HttpStatus.BAD_REQUEST, "입력받은 토큰이 비어있습니다."),
    AUTH_UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원되지 않는 토큰 형식입니다."),
    AUTH_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "필드 유효성 검증에 실패했습니다"),

    REFRESHTOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "RefreshToken을 찾지 못했습니다."),
    REFRESHTOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다"),


    //유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 USER가 존재하지 않습니다."),
    ALREADY_EXIST_USER(HttpStatus.BAD_REQUEST, "이미 존재하는 사용자입니다."),

    // 댓글(후기)
    ALREADY_COMMENTED(HttpStatus.BAD_REQUEST, "이미 해당 프로젝트에 댓글을 작성했습니다."),

    // 쿠폰
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 쿠폰을 찾을 수 없습니다"),
    COUPON_OUT_STOCK(HttpStatus.BAD_REQUEST, "쿠폰의 재고가 부족합니다"),
    ALREADY_ISSUED_COUPON(HttpStatus.CONFLICT, "이미 발급받은 쿠폰입니다"),
    INVALID_COUPON_STATUS(HttpStatus.BAD_REQUEST, "사용할 수 없는 쿠폰입니다."),
    USER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저가 사용할 쿠폰이 존재하지 않습니다."),
    INVALID_USE_COUPON(HttpStatus.BAD_REQUEST, "만료됐거나 사용한 쿠폰은 다시 사용할 수 없습니다"),
    // 주문
    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 ID의 배송지가 존재하지 않습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상품의 ID가 존재하지 않습니다."),
    INSUFFICIENT_INVENTORY(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),

    // 카카오 api
    KAKAO_PAY_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "카카오페이 서버와의 통신에 실패했습니다. 잠시 후 다시 시도해주세요."),

    // 계좌
    BANK_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자 은행 계좌를 찾을 수 없습니다.");

    private final HttpStatus errorCode;
    private final String errorMessage;

    ErrorCode(HttpStatus errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

}
