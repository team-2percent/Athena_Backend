//package goorm.athena.global;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.stereotype.Component;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.Timestamp;
//
//@Component
//@RequiredArgsConstructor
//public class h2user implements ApplicationRunner {
//
//    private final DataSource dataSource;
//
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        try (Connection conn = dataSource.getConnection()) {
//            conn.setAutoCommit(false); // 일괄 커밋 설정
//
//            try (
//                    PreparedStatement imageStmt = conn.prepareStatement("INSERT INTO image_group (id, type) VALUES (?, ?)");
//                    PreparedStatement userStmt = conn.prepareStatement(
//                            "INSERT INTO \"user\" (id, image_group_id, email, password, nickname, role, seller_introduction, link_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
//                    PreparedStatement couponStmt = conn.prepareStatement(
//                            "INSERT INTO coupon (id, title, content, price, start_at, end_at, expires_at, stock, coupon_status) " +
//                                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")
//            ) {
//                final int NUM_USERS = 1_000_000;
//                final int BATCH_SIZE = 1000;
//                final int START_ID = 32; // 시작 ID
////                final int NUM_USERS = 1_000_000 + START_ID; // 총 사용자 수 보장
//
//// image_group 삽입
//                for (int i = START_ID; i < NUM_USERS; i++) {
//                    imageStmt.setInt(1, i);
//                    imageStmt.setString(2, "USER");
//                    imageStmt.addBatch();
//
//                    if (i % BATCH_SIZE == 0) {
//                        imageStmt.executeBatch();
//                        conn.commit();
//                    }
//                }
//                imageStmt.executeBatch();
//                conn.commit();
//                System.out.println("✅ image_group 삽입 완료");
//
//// user 삽입
//                for (int i = START_ID; i < NUM_USERS; i++) {
//                    userStmt.setInt(1, i);  // user.id
//                    userStmt.setInt(2, i);  // image_group_id도 동일
//                    userStmt.setString(3, "user" + i + "@example.com");
//                    userStmt.setString(4, "123123123");
//                    userStmt.setString(5, "User" + i);
//                    userStmt.setString(6, "ROLE_USER");
//                    userStmt.setString(7, "안녕하세요! User" + i + "입니다.");
//                    userStmt.setString(8, "https://user" + i + ".example.com");
//
//                    userStmt.addBatch();
//
//                    if (i % BATCH_SIZE == 0) {
//                        userStmt.executeBatch();
//                        conn.commit();
//                        System.out.println("✅ " + i + "명 삽입 완료");
//                    }
//                }
//                userStmt.executeBatch();
//                conn.commit();
//                System.out.println("🎉 전체 유저 삽입 완료!");
////                // coupon 삽입
////                final int NUM_COUPONS = 30;
////                for (int i = 1; i <= NUM_COUPONS; i++) {
////                    couponStmt.setLong(1, i);
////                    couponStmt.setString(2, "할인 쿠폰 " + i);
////                    couponStmt.setString(3, "배치 테스트용 쿠폰입니다"); // 할인 타입
////                    couponStmt.setInt(4, 3000); //
////                    couponStmt.setTimestamp(5, Timestamp.valueOf("2025-06-01 00:00:00"));
////                    couponStmt.setTimestamp(6, Timestamp.valueOf("2025-06-01 00:00:00"));
////                    couponStmt.setTimestamp(7, Timestamp.valueOf("2025-06-01 00:00:00"));
////                    couponStmt.setInt(8, 10000);
////                    couponStmt.setString(9, "IN_PROGRESS");
////
////                    couponStmt.addBatch();
////
////                    if (i % 10 == 0) {
////                        couponStmt.executeBatch();
////                        conn.commit();
////                        System.out.println("✅ 쿠폰 " + i + "개 삽입 완료");
////                    }
////                }
////
////                couponStmt.executeBatch();
////                conn.commit();
////                System.out.println("🎉 30개 쿠폰 삽입 완료!");
//            }
//        }
//    }
//}