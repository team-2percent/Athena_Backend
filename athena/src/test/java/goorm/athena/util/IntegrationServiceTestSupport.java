package goorm.athena.util;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Transactional
@ActiveProfiles("test")
@SpringBootTest
public abstract class IntegrationServiceTestSupport {

    @Autowired
    protected DataSource dataSource;
    @Autowired protected ResourceLoader resourceLoader;


    @BeforeEach
    void setup() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(
                    conn,
                    resourceLoader.getResource("classpath:/truncate.sql")
            );

            ScriptUtils.executeSqlScript(
                    conn,
                    resourceLoader.getResource("classpath:/data.sql")
            );
        } // 여기서 conn이 자동으로 닫힘
    }
}
