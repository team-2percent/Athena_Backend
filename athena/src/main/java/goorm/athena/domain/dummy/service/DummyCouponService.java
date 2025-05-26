package goorm.athena.domain.dummy.service;

import goorm.athena.domain.coupon.dto.req.CouponCreateRequest;
import goorm.athena.domain.coupon.entity.Coupon;
import goorm.athena.domain.coupon.entity.CouponStatus;
import goorm.athena.domain.coupon.repository.CouponRepository;
import goorm.athena.global.exception.CustomException;
import goorm.athena.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DummyCouponService {

    private final JdbcTemplate jdbcTemplate;
    private final Faker faker = new Faker(new Locale("ko"));
    private final Random random = new Random();

    private static final int[] STOCK_CHOICES = {100, 200, 300, 400, 500};

    @Transactional
    public void generateDummyCoupons(int count) {
        List<Object[]> jdbcValues = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < count; i++) {
            CouponStatus status = getRandomStatus();
            LocalDateTime startAt, endAt;

            // 상태별 기간 설정
            switch (status) {
                case PREVIOUS -> {
                    startAt = now.plusDays(random.nextInt(10) + 1);
                    endAt = startAt.plusDays(random.nextInt(10) + 1);
                }
                case IN_PROGRESS -> {
                    startAt = now;
                    endAt = now.plusDays(random.nextInt(10) + 1);
                }
                case ENDED -> {
                    long daysAgo = 365 + random.nextInt(365);
                    startAt = now.minusDays(daysAgo);
                    endAt = startAt.plusDays(random.nextInt(10) + 1);
                }
                default -> throw new CustomException(ErrorCode.INVALID_COUPON_STATUS);
            }

            LocalDateTime expiresAt = endAt.plusDays(30);
            int price = (random.nextInt(10) + 1) * 1000;

            int stock = STOCK_CHOICES[random.nextInt(STOCK_CHOICES.length)];

            // 💡 할인 쿠폰 주제 기반 타이틀/내용
            String discountType = switch (random.nextInt(5)) {
                case 0 -> "첫 구매";
                case 1 -> "앱 전용";
                case 2 -> "주말 한정";
                case 3 -> "생일 축하";
                default -> "단골 고객";
            };

            String title = discountType + " " + price + "원 할인 쿠폰";
            String content = String.format(
                    "%s에서 사용 가능한 %,d원 할인 쿠폰입니다. %s 고객님을 위한 혜택이에요!",
                    faker.company().name(), price, discountType
            );

            jdbcValues.add(new Object[]{
                    title, content, price, startAt, endAt, expiresAt, stock, status.name()
            });
        }

        String sql = """
            INSERT INTO coupon (title, content, price, start_at, end_at, expires_at, stock, coupon_status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                Object[] row = jdbcValues.get(i);
                ps.setString(1, (String) row[0]);
                ps.setString(2, (String) row[1]);
                ps.setInt(3, (int) row[2]);
                ps.setObject(4, row[3]);
                ps.setObject(5, row[4]);
                ps.setObject(6, row[5]);
                ps.setInt(7, (int) row[6]);
                ps.setString(8, (String) row[7]);
            }

            @Override
            public int getBatchSize() {
                return jdbcValues.size();
            }
        });

        System.out.println("더미 쿠폰 " + count + "개 생성 완료");
    }

    private CouponStatus getRandomStatus() {
        int roll = random.nextInt(100);
        if (roll < 20) return CouponStatus.PREVIOUS;
        if (roll < 30) return CouponStatus.IN_PROGRESS;
        return CouponStatus.ENDED;
    }
}