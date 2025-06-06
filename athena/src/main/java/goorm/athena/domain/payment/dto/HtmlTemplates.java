package goorm.athena.domain.payment.dto;

public class HtmlTemplates {
    public static String kakaoSuccessHtml() {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><title>결제 성공</title></head><body>" +
                "<p>결제가 성공적으로 완료되었습니다.</p>" +
                "<script>" +
                "window.opener?.postMessage({ type: 'KAKAO_PAYMENT_SUCCESS' }, '*');" +
                "window.close();" +
                "</script></body></html>";
    }

    public static String kakaoFailHtml() {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><title>결제 실패</title></head><body>" +
                "<p>결제에 실패했습니다.</p>" +
                "<script>" +
                "window.opener?.postMessage({ type: 'KAKAO_PAYMENT_FAIL' }, '*');" +
                "window.close();" +
                "</script></body></html>";
    }
}