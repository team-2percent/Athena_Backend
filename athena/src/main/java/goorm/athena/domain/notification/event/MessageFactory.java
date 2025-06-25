package goorm.athena.domain.notification.event;

import org.springframework.stereotype.Component;

@Component
public class MessageFactory {

    public FcmMessage forLogin(String userName){
        return new FcmMessage("로그인 성공", userName + " 님, 환영합니다! 즐거운 쇼핑하세요 🛍️");
    }
    public FcmMessage forPurchaseBuyer() {
        return new FcmMessage("💸구매 완료", "상품 결제가 완료되었습니다.");
    }

    public FcmMessage forPurchaseSeller(String buyerName) {
        return new FcmMessage("💸주문 발생", buyerName + "님이 상품을 구매했습니다.");
    }

    public FcmMessage forReview(String projectName) {
        return new FcmMessage("⭐후기 등록", projectName + "에 대한 후기가 등록되었습니다.");
    }

    public FcmMessage forCoupon(String couponName) {
        return new FcmMessage("🎁쿠폰 발급", couponName + " 쿠폰이 도착했습니다.");
    }

    public record FcmMessage(String title, String body) {}
}
