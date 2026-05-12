package ru.rrtu.mental_health_system.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Создаёт PL/pgSQL-функции, триггеры и хранимые процедуры
 * после инициализации схемы БД.
 *
 * Все имена колонок согласованы с новой схемой (естественные ключи):
 *   - tests.test_code  (вместо test_id)
 *   - test_protocols.protocol_number, record_book_number, level_name, status_name
 *   - questions.question_number, answers.answer_code и т. д.
 */
@Component
@Order(0)
public class DbProceduresInitializer {

    private static final Logger log = LoggerFactory.getLogger(DbProceduresInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public DbProceduresInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        log.info("DbProceduresInitializer: создание PL/pgSQL-функций, триггеров и процедур...");

        // --- Триггер 1: валидация протокола тестирования ---
        exec("""
            CREATE OR REPLACE FUNCTION trg_check_protocol_score()
            RETURNS TRIGGER AS $$
            BEGIN
                IF NEW.level_name IS NOT NULL AND NOT EXISTS (
                    SELECT 1 FROM stress_levels WHERE level_name = NEW.level_name
                ) THEN
                    RAISE EXCEPTION 'Уровень стресса % не найден', NEW.level_name;
                END IF;
                IF NEW.total_score < 0 THEN
                    RAISE EXCEPTION 'Балл не может быть отрицательным: %', NEW.total_score;
                END IF;
                IF NEW.taken_at IS NOT NULL AND NEW.taken_at > CURRENT_TIMESTAMP THEN
                    RAISE EXCEPTION 'Дата прохождения теста не может быть в будущем: %', NEW.taken_at;
                END IF;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql
            """);
        exec("DROP TRIGGER IF EXISTS test_protocols_score_check ON test_protocols");
        exec("""
            CREATE TRIGGER test_protocols_score_check
                BEFORE INSERT OR UPDATE ON test_protocols
                FOR EACH ROW EXECUTE FUNCTION trg_check_protocol_score()
            """);

        // --- Триггер 2: автоподстановка order_number в вопросах ---
        exec("""
            CREATE OR REPLACE FUNCTION trg_auto_question_order()
            RETURNS TRIGGER AS $$
            BEGIN
                IF NEW.order_number IS NULL THEN
                    SELECT COALESCE(MAX(order_number), 0) + 1 INTO NEW.order_number
                        FROM questions WHERE test_code = NEW.test_code;
                END IF;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql
            """);
        exec("DROP TRIGGER IF EXISTS questions_auto_order ON questions");
        exec("""
            CREATE TRIGGER questions_auto_order
                BEFORE INSERT ON questions
                FOR EACH ROW EXECUTE FUNCTION trg_auto_question_order()
            """);

        // --- Триггер 3: автоподстановка order_number в вариантах ответов ---
        exec("""
            CREATE OR REPLACE FUNCTION trg_auto_answer_order()
            RETURNS TRIGGER AS $$
            BEGIN
                IF NEW.order_number IS NULL THEN
                    SELECT COALESCE(MAX(order_number), 0) + 1 INTO NEW.order_number
                        FROM answers WHERE question_number = NEW.question_number;
                END IF;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql
            """);
        exec("DROP TRIGGER IF EXISTS answers_auto_order ON answers");
        exec("""
            CREATE TRIGGER answers_auto_order
                BEFORE INSERT ON answers
                FOR EACH ROW EXECUTE FUNCTION trg_auto_answer_order()
            """);

        // --- Триггер 4: запрет удаления теста с протоколами ---
        exec("""
            CREATE OR REPLACE FUNCTION trg_protect_test_with_protocols()
            RETURNS TRIGGER AS $$
            DECLARE v_count BIGINT;
            BEGIN
                IF TG_OP = 'DELETE' THEN
                    SELECT COUNT(*) INTO v_count FROM test_protocols WHERE test_code = OLD.test_code;
                    IF v_count > 0 THEN
                        RAISE EXCEPTION 'Нельзя удалить тест, по которому есть % протоколов', v_count;
                    END IF;
                    RETURN OLD;
                END IF;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql
            """);
        exec("DROP TRIGGER IF EXISTS tests_protect_delete ON tests");
        exec("""
            CREATE TRIGGER tests_protect_delete
                BEFORE DELETE ON tests
                FOR EACH ROW EXECUTE FUNCTION trg_protect_test_with_protocols()
            """);

        // --- Функция: расчёт уровня стресса по проценту от максимума ---
        exec("""
            CREATE OR REPLACE FUNCTION fn_get_stress_level(p_percent SMALLINT)
            RETURNS VARCHAR AS $$
            DECLARE v_name VARCHAR;
            BEGIN
                SELECT level_name INTO v_name
                  FROM stress_levels
                 WHERE p_percent BETWEEN min_percent AND max_percent
                 ORDER BY min_percent LIMIT 1;
                RETURN v_name;
            END;
            $$ LANGUAGE plpgsql STABLE
            """);

        // --- Процедура: сохранение протокола тестирования ---
        exec("""
            CREATE OR REPLACE PROCEDURE sp_save_test_protocol(
                p_record_book_number BIGINT,
                p_test_code VARCHAR,
                p_total_score SMALLINT,
                p_max_score SMALLINT,
                OUT p_protocol_number BIGINT) AS $$
            DECLARE
                v_percent SMALLINT;
                v_level_name VARCHAR;
            BEGIN
                IF p_max_score IS NULL OR p_max_score = 0 THEN
                    v_percent := 0;
                ELSE
                    v_percent := ROUND(p_total_score * 100.0 / p_max_score)::SMALLINT;
                END IF;
                v_level_name := fn_get_stress_level(v_percent);
                IF v_level_name IS NULL THEN
                    RAISE EXCEPTION 'Не удалось определить уровень стресса для % процентов', v_percent;
                END IF;
                INSERT INTO test_protocols(record_book_number, test_code, taken_at,
                                           total_score, level_name, status_name)
                    VALUES (p_record_book_number, p_test_code, CURRENT_TIMESTAMP,
                            p_total_score, v_level_name, 'Завершён')
                    RETURNING protocol_number INTO p_protocol_number;
            END;
            $$ LANGUAGE plpgsql
            """);

        // --- Процедура: запись в журнал аудита ---
        exec("""
            CREATE OR REPLACE PROCEDURE sp_audit_log(
                p_login VARCHAR,
                p_operation VARCHAR,
                p_table_name VARCHAR,
                p_details TEXT) AS $$
            BEGIN
                INSERT INTO audit_log(action_time, login, operation, table_name, details)
                VALUES (CURRENT_TIMESTAMP, p_login, p_operation, p_table_name, p_details);
            END;
            $$ LANGUAGE plpgsql
            """);

        // ============================================================
        // Хранимые процедуры лабораторной работы №3
        // (Адаптация типового задания «товары/поставки» к тематике
        //  мониторинга ментального здоровья: «тесты/протоколы тестирования»)
        // ============================================================

        // ЛР3 / 1. Снизить total_score у всех протоколов на N% — моделирует
        // снижение уровня стресса после психотерапевтической программы.
        execIgnore("DROP PROCEDURE IF EXISTS sp_lab3_decrease_scores(INT)");
        exec("""
            CREATE OR REPLACE PROCEDURE sp_lab3_decrease_scores(p_percent INT)
            LANGUAGE plpgsql AS $$
            BEGIN
                IF p_percent IS NULL OR p_percent < 0 OR p_percent > 100 THEN
                    RAISE EXCEPTION 'Процент должен быть в диапазоне 0..100';
                END IF;
                UPDATE test_protocols
                   SET total_score = GREATEST(0,
                       (total_score * (100 - p_percent) / 100)::SMALLINT);
            END;
            $$
            """);

        // ЛР3 / 2. Добавить новый психологический тест.
        execIgnore("DROP PROCEDURE IF EXISTS sp_lab3_add_test(VARCHAR, VARCHAR, TEXT, TEXT)");
        execIgnore("DROP PROCEDURE IF EXISTS sp_lab3_add_test(VARCHAR, TEXT, TEXT)");
        exec("""
            CREATE OR REPLACE PROCEDURE sp_lab3_add_test(
                p_test_code VARCHAR,
                p_name VARCHAR,
                p_description TEXT,
                p_instructions TEXT)
            LANGUAGE plpgsql AS $$
            BEGIN
                INSERT INTO tests(test_code, name, description, instructions, is_active)
                VALUES (p_test_code, p_name, p_description, p_instructions, TRUE);
            END;
            $$
            """);

        // ЛР3 / 3. Удалить протоколы заданного студента по заданному тесту.
        execIgnore("DROP PROCEDURE IF EXISTS sp_lab3_delete_protocols(VARCHAR, VARCHAR)");
        exec("""
            CREATE OR REPLACE PROCEDURE sp_lab3_delete_protocols(
                p_student_login VARCHAR,
                p_test_code VARCHAR)
            LANGUAGE plpgsql AS $$
            BEGIN
                DELETE FROM test_protocols p
                 USING students s, users u
                 WHERE p.record_book_number = s.record_book_number
                   AND s.login = u.login
                   AND p.test_code = p_test_code
                   AND u.login = p_student_login;
            END;
            $$
            """);

        // ЛР3 / 4. Удалить заданный тест с возвратом кода ошибки:
        //   0 — удалено, 1 — есть зависимые протоколы, 2 — теста нет.
        execIgnore("DROP FUNCTION IF EXISTS fn_lab3_delete_test(VARCHAR)");
        exec("""
            CREATE OR REPLACE FUNCTION fn_lab3_delete_test(p_test_code VARCHAR)
            RETURNS INT AS $$
            DECLARE
                v_count BIGINT;
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM tests WHERE test_code = p_test_code) THEN
                    RETURN 2;
                END IF;
                SELECT COUNT(*) INTO v_count FROM test_protocols WHERE test_code = p_test_code;
                IF v_count > 0 THEN
                    RETURN 1;
                END IF;
                DELETE FROM answers WHERE question_number IN
                    (SELECT question_number FROM questions WHERE test_code = p_test_code);
                DELETE FROM questions WHERE test_code = p_test_code;
                DELETE FROM tests WHERE test_code = p_test_code;
                RETURN 0;
            END;
            $$ LANGUAGE plpgsql
            """);

        // ЛР3 / 5. Вернуть количество прохождений заданного теста.
        // Если p_student_login пуст/NULL — считаем по всем студентам;
        // если указан логин — фильтруем результаты только по этому студенту.
        // Коды возврата через OUT-параметр:
        //   -1 — теста с таким шифром нет;
        //   -2 — студента с таким логином нет (только если логин указан);
        //  ≥ 0 — фактическое количество прохождений.
        execIgnore("DROP PROCEDURE IF EXISTS sp_lab3_test_total(VARCHAR)");
        execIgnore("DROP PROCEDURE IF EXISTS sp_lab3_test_total(VARCHAR, VARCHAR)");
        exec("""
            CREATE OR REPLACE PROCEDURE sp_lab3_test_total(
                p_test_code VARCHAR,
                p_student_login VARCHAR,
                OUT p_total BIGINT)
            LANGUAGE plpgsql AS $$
            DECLARE
                v_rbn BIGINT;
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM tests WHERE test_code = p_test_code) THEN
                    p_total := -1;
                    RETURN;
                END IF;
                IF p_student_login IS NULL OR length(trim(p_student_login)) = 0 THEN
                    -- Подсчёт по всем студентам
                    SELECT COUNT(*) INTO p_total
                      FROM test_protocols
                     WHERE test_code = p_test_code;
                ELSE
                    -- Фильтр по конкретному студенту
                    SELECT record_book_number INTO v_rbn
                      FROM students
                     WHERE login = p_student_login;
                    IF v_rbn IS NULL THEN
                        p_total := -2;
                        RETURN;
                    END IF;
                    SELECT COUNT(*) INTO p_total
                      FROM test_protocols
                     WHERE test_code = p_test_code
                       AND record_book_number = v_rbn;
                END IF;
                IF p_total IS NULL THEN
                    p_total := 0;
                END IF;
            END;
            $$
            """);

        // ЛР3 / 6. CRUD для таблицы психологов.
        execIgnore("DROP PROCEDURE IF EXISTS sp_lab3_psychologist_update("
                + "BIGINT, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR, VARCHAR)");
        exec("""
            CREATE OR REPLACE PROCEDURE sp_lab3_psychologist_update(
                p_personnel_number BIGINT,
                p_last_name VARCHAR,
                p_first_name VARCHAR,
                p_middle_name VARCHAR,
                p_position VARCHAR,
                p_email VARCHAR,
                p_phone VARCHAR)
            LANGUAGE plpgsql AS $$
            BEGIN
                UPDATE psychologists
                   SET last_name = p_last_name,
                       first_name = p_first_name,
                       middle_name = p_middle_name,
                       position = p_position,
                       email = p_email,
                       phone = p_phone
                 WHERE personnel_number = p_personnel_number;
            END;
            $$
            """);

        log.info("DbProceduresInitializer: PL/pgSQL-функции, триггеры и процедуры созданы.");
    }

    private void execIgnore(String sql) {
        try { jdbcTemplate.execute(sql); } catch (Exception ignored) { }
    }

    private void exec(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            log.error("Ошибка выполнения SQL: {}", e.getMessage());
            log.debug("SQL:\n{}", sql);
            throw e;
        }
    }
}
