package goorm.athena.domain.dummy.service;

import goorm.athena.domain.project.entity.PlanName;
import goorm.athena.domain.project.entity.PlatformPlan;
import goorm.athena.domain.project.repository.PlatformPlanRepository;
import goorm.athena.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DummyService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformPlanRepository platformPlanRepository;

    private final Faker fakerKo = new Faker(new Locale("ko"));
    private final Faker fakerEn = new Faker(Locale.ENGLISH);

    @Transactional
    public void generateDummyUsers(int count) {
        insertPlatformPlansIfNotExists();

        boolean adminExists = checkAdminExists();
        int actualCount = adminExists ? count : count - 1;

        List<UserInfo> users = new ArrayList<>();

        if (!adminExists) {
            users.add(generateUserInfo("admin@admin.com", "admin", "admin", Role.ROLE_ADMIN));
        }

        for (int i = 0; i < actualCount; i++) {
            String email = generateEmail();
            String password = "123";
            String nickname = fakerKo.name().lastName() + fakerKo.name().firstName();
            users.add(generateUserInfo(email, password, nickname, Role.ROLE_USER));
        }

        List<Long> userIds = insertUsersInBulk(users);
        insertBankAccounts(userIds, users.stream().map(UserInfo::nickname).toList());
        insertDeliveryInfos(userIds);
    }

    private UserInfo generateUserInfo(String email, String password, String nickname, Role role) {
        return new UserInfo(
                email,
                password,
                nickname,
                role.name(),
                fakerKo.lorem().sentence(10),
                fakerEn.internet().url()
        );
    }

    private String generateEmail() {
        return "user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    private String generateFakeAccountNumber() {
        return String.format("%04d-%02d-%07d",
                fakerKo.number().numberBetween(1000, 9999),
                fakerKo.number().numberBetween(10, 99),
                fakerKo.number().numberBetween(1000000, 9999999)
        );
    }

    private List<Long> insertUsersInBulk(List<UserInfo> users) {
        String sql = "INSERT INTO `user` (email, password, nickname, role, seller_introduction, link_url) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                UserInfo user = users.get(i);
                ps.setString(1, user.email());
                ps.setString(2, user.password());
                ps.setString(3, user.nickname());
                ps.setString(4, user.role());
                ps.setString(5, user.introduction());
                ps.setString(6, user.link());
            }

            @Override
            public int getBatchSize() {
                return users.size();
            }
        });

        Long lastId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM `user`", Long.class);
        long startId = lastId - users.size() + 1;
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            ids.add(startId + i);
        }
        return ids;
    }

    private void insertBankAccounts(List<Long> userIds, List<String> nicknames) {
        String sql = "INSERT INTO bank_account (user_id, account_number, account_holder, bank_name, is_default) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                ps.setLong(1, userIds.get(i));
                ps.setString(2, generateFakeAccountNumber());
                ps.setString(3, nicknames.get(i));
                ps.setString(4, "카카오뱅크");
                ps.setBoolean(5, true);
            }

            @Override
            public int getBatchSize() {
                return userIds.size();
            }
        });
    }

    private void insertDeliveryInfos(List<Long> userIds) {
        String sql = "INSERT INTO delivery_info (user_id, zipcode, address, detail_address, is_default) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws java.sql.SQLException {
                ps.setLong(1, userIds.get(i));
                ps.setString(2, fakerKo.numerify("###-###"));
                ps.setString(3, fakerKo.address().cityName() + " " + fakerKo.address().streetName());
                ps.setString(4, fakerKo.address().buildingNumber() + "호");
                ps.setBoolean(5, true);
            }

            @Override
            public int getBatchSize() {
                return userIds.size();
            }
        });
    }

    private boolean checkAdminExists() {
        String sql = "SELECT EXISTS (SELECT 1 FROM `user` WHERE role = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, Role.ROLE_ADMIN.name()));
    }

    private void insertPlatformPlansIfNotExists() {
        if (!platformPlanRepository.existsByName(PlanName.BASIC)) {
            platformPlanRepository.saveAll(List.of(
                    PlatformPlan.builder()
                            .name(PlanName.BASIC)
                            .platformFeeRate(0.05)
                            .pgFeeRate(0.03)
                            .vatRate(0.10)
                            .description("기본 요금제 - 최소 기능 제공")
                            .build(),
                    PlatformPlan.builder()
                            .name(PlanName.PRO)
                            .platformFeeRate(0.09)
                            .pgFeeRate(0.03)
                            .vatRate(0.10)
                            .description("프로 요금제 - 마케팅 도구 포함")
                            .build(),
                    PlatformPlan.builder()
                            .name(PlanName.PREMIUM)
                            .platformFeeRate(0.15)
                            .pgFeeRate(0.03)
                            .vatRate(0.10)
                            .description("프리미엄 요금제 - 전체 기능 제공 및 우선 지원")
                            .build()
            ));
            System.out.println("✅ PlatformPlan 초기 데이터 삽입 완료");
        }
    }

    private record UserInfo(
            String email,
            String password,
            String nickname,
            String role,
            String introduction,
            String link
    ) {}
}