package floorida.example.floorida.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Lightweight, idempotent schema patches for production DBs.
 *
 * 목적: 운영에서 수동 SQL 없이도 신규 컬럼을 안전하게 추가.
 */
@Component
public class SchemaMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private final DataSource dataSource;

    public SchemaMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            String product = meta.getDatabaseProductName();

            // Postgres / H2 호환 SQL (둘 다 IF NOT EXISTS 지원)
            // 다른 DB를 쓰게 되면 여기서 분기 추가하면 됩니다.
            if (product != null && (product.toLowerCase().contains("postgres") || product.toLowerCase().contains("h2"))) {
                try (var stmt = connection.createStatement()) {
                    stmt.execute("ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS last_daily_login_reward_date DATE");
                    stmt.execute("ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS daily_login_streak INT");
                    stmt.execute("ALTER TABLE user_profiles ALTER COLUMN daily_login_streak SET DEFAULT 0");
                    stmt.execute("UPDATE user_profiles SET daily_login_streak = 0 WHERE daily_login_streak IS NULL");
                }
                log.info("SchemaMigrationRunner: ensured user_profiles columns for login streak");
            } else {
                log.info("SchemaMigrationRunner: skipped (db={})", product);
            }
        } catch (Exception e) {
            // 마이그레이션 실패가 곧바로 서버 부팅 실패로 이어지지 않게 방어
            // (권한 부족/테이블 미존재 등 환경에 따라 달라질 수 있음)
            log.warn("SchemaMigrationRunner: schema patch skipped due to error: {}", e.getMessage());
        }
    }
}
