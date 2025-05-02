package goorm.athena.domain.payment.dto.res;

public class PaymentResponse {

    private String tid;                       // 결제 고유 번호
    private String nextRedirectAppUrl;        // 모바일 앱 결제 URL
    private String nextRedirectMobileUrl;     // 모바일 웹 결제 URL
    private String nextRedirectPcUrl;         // PC 웹 결제 URL
    private String androidAppScheme;          // 안드로이드 앱 스킴
    private String iosAppScheme;              // iOS 앱 스킴
    private String createdAt;                 // 결제 준비 요청 시간
}