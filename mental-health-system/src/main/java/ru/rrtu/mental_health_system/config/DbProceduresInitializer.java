package ru.rrtu.mental_health_system.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Создаёт PL/pgSQL-функции, триггеры и хранимые процедуры после
 * инициализации схемы БД. Имена согласованы с моделью данных из ПЗ:
 *   grades(grade_name), indicators(indicator_name),
 *   test_protocols(grade_name), recommendations(grade_name),
 *   item_results, test_notes(protocol_number), consultations,
 *   students(curator_personnel_number), tests(instruction, indicator_name).
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

        // --- Триггер 1: валидация протокола тестирования (градация, балл, дата) ---
        exec("""
            CREATE OR REPLACE FUNCTION trg_check_test_protocol_score()
            RETURNS TRIGGER AS $$
            BEGIN
                IF NEW.grade_name IS NOT NULL AND NOT EXISTS (
                    SELECT 1 FROM grades WHERE grade_name = NEW.grade_name
                ) THEN
                    RAISE EXCEPTION 'Градация % не найдена', NEW.grade_name;
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
                FOR EACH ROW EXECUTE FUNCTION trg_check_test_protocol_score()
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
                SELECT COUNT(*) INTO v_count FROM test_protocols WHERE test_code = OLD.test_code;
                IF v_count > 0 THEN
                    RAISE EXCEPTION 'Нельзя удалить тест, по которому есть % протоколов', v_count;
                END IF;
                RETURN OLD;
            END;
            $$ LANGUAGE plpgsql
            """);
        exec("DROP TRIGGER IF EXISTS tests_protect_delete ON tests");
        exec("""
            CREATE TRIGGER tests_protect_delete
                BEFORE DELETE ON tests
                FOR EACH ROW EXECUTE FUNCTION trg_protect_test_with_protocols()
            """);

        // --- Функция: расчёт названия градации по проценту от максимума ---
        exec("""
            CREATE OR REPLACE FUNCTION fn_get_grade(p_percent SMALLINT)
            RETURNS VARCHAR AS $$
            DECLARE v_name VARCHAR;
            BEGIN
                SELECT grade_name INTO v_name
                  FROM grades
                 WHERE p_percent BETWEEN min_percent AND max_percent
                 ORDER BY min_percent LIMIT 1;
                RETURN v_name;
            END;
            $$ LANGUAGE plpgsql STABLE
            """);

        // --- Процедура: атомарное сохранение протокола + определение градации ---
        exec("""
            CREATE OR REPLACE PROCEDURE sp_save_test_protocol(
                p_record_book_number BIGINT,
                p_test_code VARCHAR,
                p_total_score SMALLINT,
                p_max_score SMALLINT,
                OUT p_protocol_number BIGINT) AS $$
            DECLARE
                v_percent SMALLINT;
                v_grade_name VARCHAR;
            BEGIN
                IF p_max_score IS NULL OR p_max_score = 0 THEN
                    v_percent := 0;
                ELSE
                    v_percent := ROUND(p_total_score * 100.0 / p_max_score)::SMALLINT;
                END IF;
                v_grade_name := fn_get_grade(v_percent);
                IF v_grade_name IS NULL THEN
                    RAISE EXCEPTION 'Не удалось определить градацию для % процентов', v_percent;
                END IF;
                INSERT INTO test_protocols(record_book_number, test_code, taken_at,
                                           total_score, grade_name)
                    VALUES (p_record_book_number, p_test_code, CURRENT_TIMESTAMP,
                            p_total_score, v_grade_name)
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

        // --- Процедура: регистрация студента (user + профиль за один шаг) ---
        exec("""
            CREATE OR REPLACE PROCEDURE sp_register_student(
                p_login VARCHAR,
                p_password_hash VARCHAR,
                p_record_book_number BIGINT,
                p_last_name VARCHAR,
                p_first_name VARCHAR,
                p_middle_name VARCHAR,
                p_group_name VARCHAR) AS $$
            BEGIN
                INSERT INTO users(login, password_hash, role_name, created_at)
                VALUES (p_login, p_password_hash, 'STUDENT', CURRENT_TIMESTAMP);
                INSERT INTO students(record_book_number, login, last_name, first_name,
                                     middle_name, group_name, risk_group)
                VALUES (p_record_book_number, p_login, p_last_name, p_first_name,
                        p_middle_name, p_group_name, 'нет');
                CALL sp_audit_log(p_login, 'INSERT', 'students',
                    'Регистрация студента ' || p_last_name || ' ' || p_first_name);
            END;
            $$ LANGUAGE plpgsql
            """);

        // --- Процедуры администрирования тестов (с проверками целостности) ---
        exec("""
            CREATE OR REPLACE PROCEDURE sp_admin_create_test(
                p_test_code VARCHAR, p_name VARCHAR, p_description TEXT,
                p_instruction TEXT, p_author BIGINT, p_indicator VARCHAR) AS $$
            BEGIN
                INSERT INTO tests(test_code, name, description, instruction,
                                  is_active, author_personnel_number, indicator_name)
                VALUES (p_test_code, p_name, p_description, p_instruction,
                        TRUE, p_author, p_indicator);
                CALL sp_audit_log(NULL, 'INSERT', 'tests', 'Создан тест ' || p_test_code);
            END;
            $$ LANGUAGE plpgsql
            """);
        exec("""
            CREATE OR REPLACE PROCEDURE sp_admin_update_test(
                p_test_code VARCHAR, p_name VARCHAR, p_description TEXT,
                p_instruction TEXT, p_is_active BOOLEAN,
                p_author BIGINT, p_indicator VARCHAR) AS $$
            BEGIN
                UPDATE tests SET name = p_name, description = p_description,
                       instruction = p_instruction, is_active = p_is_active,
                       author_personnel_number = p_author, indicator_name = p_indicator
                 WHERE test_code = p_test_code;
                CALL sp_audit_log(NULL, 'UPDATE', 'tests', 'Изменён тест ' || p_test_code);
            END;
            $$ LANGUAGE plpgsql
            """);
        exec("""
            CREATE OR REPLACE PROCEDURE sp_admin_delete_test(p_test_code VARCHAR) AS $$
            DECLARE v_count BIGINT;
            BEGIN
                SELECT COUNT(*) INTO v_count FROM test_protocols WHERE test_code = p_test_code;
                IF v_count > 0 THEN
                    RAISE EXCEPTION 'Нельзя удалить тест с % протоколами', v_count;
                END IF;
                DELETE FROM answers WHERE question_number IN
                    (SELECT question_number FROM questions WHERE test_code = p_test_code);
                DELETE FROM questions WHERE test_code = p_test_code;
                DELETE FROM tests WHERE test_code = p_test_code;
                CALL sp_audit_log(NULL, 'DELETE', 'tests', 'Удалён тест ' || p_test_code);
            END;
            $$ LANGUAGE plpgsql
            """);

        // --- Функция: агрегированная динамика результатов студента ---
        exec("""
            CREATE OR REPLACE FUNCTION fn_student_dynamics(p_record_book_number BIGINT)
            RETURNS TABLE(avg_score NUMERIC, min_score SMALLINT, max_score SMALLINT, cnt BIGINT)
            AS $$
            BEGIN
                RETURN QUERY
                SELECT ROUND(AVG(total_score), 2), MIN(total_score), MAX(total_score), COUNT(*)
                  FROM test_protocols
                 WHERE record_book_number = p_record_book_number;
            END;
            $$ LANGUAGE plpgsql STABLE
            """);

        // --- Процедура: закрепление психолога-куратора за студентом ---
        exec("""
            CREATE OR REPLACE PROCEDURE sp_assign_curator_psychologist(
                p_record_book_number BIGINT,
                p_personnel_number BIGINT) AS $$
            BEGIN
                UPDATE students SET curator_personnel_number = p_personnel_number
                 WHERE record_book_number = p_record_book_number;
                INSERT INTO consultations(personnel_number, record_book_number,
                                          consultation_date, consultation_text)
                VALUES (p_personnel_number, p_record_book_number, CURRENT_DATE,
                        'Назначен куратором. Начато наблюдение за студентом.');
                CALL sp_audit_log(NULL, 'UPDATE', 'students',
                    'Назначен куратор ' || p_personnel_number ||
                    ' студенту ' || p_record_book_number);
            END;
            $$ LANGUAGE plpgsql
            """);

        // ============================================================
        // Хранимые процедуры лабораторной работы №3
        // ============================================================

        // ЛР3 / 1. Снизить total_score у всех протоколов на N%.
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
                p_instruction TEXT)
            LANGUAGE plpgsql AS $$
            BEGIN
                INSERT INTO tests(test_code, name, description, instruction, is_active)
                VALUES (p_test_code, p_name, p_description, p_instruction, TRUE);
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
                 USING students s
                 WHERE p.record_book_number = s.record_book_number
                   AND p.test_code = p_test_code
                   AND s.login = p_student_login;
            END;
            $$
            """);

        // ЛР3 / 4. Удалить заданный тест с кодом возврата (0/1/2).
        execIgnore("DROP FUNCTION IF EXISTS fn_lab3_delete_test(VARCHAR)");
        exec("""
            CREATE OR REPLACE FUNCTION fn_lab3_delete_test(p_test_code VARCHAR)
            RETURNS INT AS $$
            DECLARE v_count BIGINT;
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

        // ЛР3 / 5. Количество прохождений теста (опц. по студенту).
        execIgnore("DROP PROCEDURE IF EXISTS sp_lab3_test_total(VARCHAR)");
        execIgnore("DROP PROCEDURE IF EXISTS sp_lab3_test_total(VARCHAR, VARCHAR)");
        exec("""
            CREATE OR REPLACE PROCEDURE sp_lab3_test_total(
                p_test_code VARCHAR,
                p_student_login VARCHAR,
                OUT p_total BIGINT)
            LANGUAGE plpgsql AS $$
            DECLARE v_rbn BIGINT;
            BEGIN
                IF NOT EXISTS (SELECT 1 FROM tests WHERE test_code = p_test_code) THEN
                    p_total := -1; RETURN;
                END IF;
                IF p_student_login IS NULL OR length(trim(p_student_login)) = 0 THEN
                    SELECT COUNT(*) INTO p_total FROM test_protocols WHERE test_code = p_test_code;
                ELSE
                    SELECT record_book_number INTO v_rbn FROM students WHERE login = p_student_login;
                    IF v_rbn IS NULL THEN p_total := -2; RETURN; END IF;
                    SELECT COUNT(*) INTO p_total FROM test_protocols
                     WHERE test_code = p_test_code AND record_book_number = v_rbn;
                END IF;
                IF p_total IS NULL THEN p_total := 0; END IF;
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
                p_specialization VARCHAR,
                p_email VARCHAR,
                p_phone VARCHAR)
            LANGUAGE plpgsql AS $$
            BEGIN
                UPDATE psychologists
                   SET last_name = p_last_name,
                       first_name = p_first_name,
                       middle_name = p_middle_name,
                       specialization = p_specialization,
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
