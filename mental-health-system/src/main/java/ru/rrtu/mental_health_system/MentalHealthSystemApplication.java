package ru.rrtu.mental_health_system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

@SpringBootApplication
public class MentalHealthSystemApplication {

    private static final Logger log = LoggerFactory.getLogger(MentalHealthSystemApplication.class);

    public static void main(String[] args) {
        runOneTimeSchemaMigration();
        SpringApplication.run(MentalHealthSystemApplication.class, args);
    }

    /**
     * Однократная миграция структуры БД: переход на естественные первичные
     * ключи (см. ПЗ, раздел 2). Если в БД присутствуют таблицы со старой
     * структурой (с суррогатными role_id, user_id, test_id и т. п.) —
     * они удаляются, и затем Hibernate + schema.sql пересоздают их в
     * новой форме (с PK = role_name, login, test_code, ...).
     *
     * Миграция срабатывает только при наличии старой структуры; на
     * чистой БД и на уже мигрированной — no-op.
     */
    private static void runOneTimeSchemaMigration() {
        Properties props = new Properties();
        try (InputStream in = MentalHealthSystemApplication.class
                .getResourceAsStream("/application.properties")) {
            if (in == null) return;
            props.load(in);
        } catch (Exception e) {
            log.warn("Не удалось прочитать application.properties для миграции: {}", e.getMessage());
            return;
        }
        String url = props.getProperty("spring.datasource.url");
        String user = props.getProperty("spring.datasource.username");
        String pass = props.getProperty("spring.datasource.password");
        if (url == null) return;

        try (Connection c = DriverManager.getConnection(url, user, pass);
             Statement s = c.createStatement()) {

            boolean hasOld = oldStructureDetected(s);
            if (!hasOld) return;

            log.warn("Обнаружена старая структура БД (суррогатные id) — выполняется миграция на естественные ключи.");
            // Удаляем все таблицы и представления — Hibernate + schema.sql пересоздадут их.
            s.execute("DROP VIEW IF EXISTS v_student_dynamics CASCADE");
            String[] tables = {
                "audit_log", "psychologist_notes", "psychologist_specializations",
                "result_answers", "test_protocols", "test_results",
                "recommendations", "answers", "questions", "tests",
                "stress_levels", "result_statuses", "test_categories",
                "students", "psychologists", "users", "roles"
            };
            for (String t : tables) {
                try { s.execute("DROP TABLE IF EXISTS " + t + " CASCADE"); }
                catch (Exception ignored) {}
            }
            log.info("Старые таблицы удалены, схема будет создана заново согласно ПЗ.");
        } catch (Exception e) {
            log.debug("Миграция пропущена: {}", e.getMessage());
        }
    }

    /** Признак старой схемы: наличие колонки users.user_id или roles.role_id. */
    private static boolean oldStructureDetected(Statement s) {
        try (var rs = s.executeQuery("""
                SELECT 1 FROM information_schema.columns
                 WHERE (table_name='users' AND column_name='user_id')
                    OR (table_name='roles' AND column_name='role_id')
                    OR (table_name='tests' AND column_name='test_id')
                    OR (table_name='stress_levels' AND column_name='level_id')
                """)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}
