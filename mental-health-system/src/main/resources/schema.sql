-- =====================================================================
-- Схема БД «Мониторинг ментального здоровья» (PostgreSQL).
-- Модель данных согласно ПЗ (разделы 2.1–2.2): 16 таблиц,
-- естественные первичные ключи, НФБК.
--   Показатель (indicators)  — измеряемая характеристика (Стресс, Тревога…)
--   Градация   (grades)      — степень выраженности по доле баллов (Низкий…)
--   Кураторство              — students.curator_personnel_number
--   Консультация / Заметка по тестированию — отдельные слабые сущности.
-- Junction-таблицы с составным PK: psychologist_specializations, item_results.
-- =====================================================================

-- 1. Роли пользователей
CREATE TABLE IF NOT EXISTS roles (
    role_name VARCHAR(30) PRIMARY KEY,
    description TEXT
);

-- 2. Учётные записи
CREATE TABLE IF NOT EXISTS users (
    login VARCHAR(50) PRIMARY KEY,
    password_hash VARCHAR(255) NOT NULL,
    role_name VARCHAR(30) NOT NULL REFERENCES roles(role_name),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Показатели психодиагностики (Стресс, Тревога, Депрессия, Выгорание)
CREATE TABLE IF NOT EXISTS indicators (
    indicator_name VARCHAR(50) PRIMARY KEY,
    description TEXT
);

-- 4. Градации выраженности показателя (по доле баллов от максимума)
CREATE TABLE IF NOT EXISTS grades (
    grade_name VARCHAR(30) PRIMARY KEY,
    min_percent SMALLINT NOT NULL,
    max_percent SMALLINT NOT NULL,
    CHECK (min_percent >= 0 AND min_percent <= 100),
    CHECK (max_percent >= 0 AND max_percent <= 100),
    CHECK (min_percent <= max_percent)
);

-- 5. Психологи
CREATE TABLE IF NOT EXISTS psychologists (
    personnel_number BIGINT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE REFERENCES users(login) ON DELETE CASCADE,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    specialization VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20)
);

-- 6. Студенты (с куратором, психотипом и группой риска)
CREATE TABLE IF NOT EXISTS students (
    record_book_number BIGINT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE REFERENCES users(login) ON DELETE CASCADE,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    group_name VARCHAR(20) NOT NULL,
    psych_type VARCHAR(50),
    risk_group VARCHAR(20),
    curator_personnel_number BIGINT REFERENCES psychologists(personnel_number) ON DELETE SET NULL,
    email VARCHAR(100),
    phone VARCHAR(20)
);

-- 7. Тесты (методики); измеряют показатель, имеют автора-психолога
CREATE TABLE IF NOT EXISTS tests (
    test_code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    instruction TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    author_personnel_number BIGINT REFERENCES psychologists(personnel_number),
    indicator_name VARCHAR(50) REFERENCES indicators(indicator_name)
);

-- 8. Вопросы
CREATE TABLE IF NOT EXISTS questions (
    question_number BIGSERIAL PRIMARY KEY,
    test_code VARCHAR(20) NOT NULL REFERENCES tests(test_code) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    order_number SMALLINT NOT NULL
);

-- 9. Варианты ответов
CREATE TABLE IF NOT EXISTS answers (
    answer_code BIGSERIAL PRIMARY KEY,
    question_number BIGINT NOT NULL REFERENCES questions(question_number) ON DELETE CASCADE,
    answer_text VARCHAR(200) NOT NULL,
    score SMALLINT NOT NULL CHECK (score >= 0),
    order_number SMALLINT NOT NULL
);

-- 10. Протоколы тестирования (заключение — градация)
CREATE TABLE IF NOT EXISTS test_protocols (
    protocol_number BIGSERIAL PRIMARY KEY,
    record_book_number BIGINT NOT NULL REFERENCES students(record_book_number) ON DELETE CASCADE,
    test_code VARCHAR(20) NOT NULL REFERENCES tests(test_code),
    taken_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_score SMALLINT NOT NULL CHECK (total_score >= 0),
    grade_name VARCHAR(30) REFERENCES grades(grade_name)
);

-- 11. Результат по пункту (составной PK: протокол + вопрос)
CREATE TABLE IF NOT EXISTS item_results (
    protocol_number BIGINT NOT NULL REFERENCES test_protocols(protocol_number) ON DELETE CASCADE,
    question_number BIGINT NOT NULL REFERENCES questions(question_number),
    answer_code BIGINT NOT NULL REFERENCES answers(answer_code),
    PRIMARY KEY (protocol_number, question_number)
);

-- 12. Рекомендации (привязаны к градации, имеют автора)
CREATE TABLE IF NOT EXISTS recommendations (
    recommendation_code BIGSERIAL PRIMARY KEY,
    grade_name VARCHAR(30) NOT NULL REFERENCES grades(grade_name),
    recommendation_text TEXT NOT NULL,
    order_number SMALLINT,
    author_personnel_number BIGINT REFERENCES psychologists(personnel_number)
);

-- 13. Специализации психологов (junction M:N: психолог × показатель)
CREATE TABLE IF NOT EXISTS psychologist_specializations (
    personnel_number BIGINT NOT NULL REFERENCES psychologists(personnel_number) ON DELETE CASCADE,
    indicator_name VARCHAR(50) NOT NULL REFERENCES indicators(indicator_name) ON DELETE CASCADE,
    PRIMARY KEY (personnel_number, indicator_name)
);

-- 14. Консультации (датированные беседы психолог × студент)
CREATE TABLE IF NOT EXISTS consultations (
    consultation_number BIGSERIAL PRIMARY KEY,
    personnel_number BIGINT NOT NULL REFERENCES psychologists(personnel_number) ON DELETE CASCADE,
    record_book_number BIGINT NOT NULL REFERENCES students(record_book_number) ON DELETE CASCADE,
    consultation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    consultation_text TEXT
);

-- 15. Заметки по тестированию (привязаны к протоколу)
CREATE TABLE IF NOT EXISTS test_notes (
    note_number BIGSERIAL PRIMARY KEY,
    personnel_number BIGINT NOT NULL REFERENCES psychologists(personnel_number) ON DELETE CASCADE,
    protocol_number BIGINT NOT NULL REFERENCES test_protocols(protocol_number) ON DELETE CASCADE,
    note_date DATE NOT NULL DEFAULT CURRENT_DATE,
    note_text TEXT NOT NULL
);

-- 16. Журнал аудита
CREATE TABLE IF NOT EXISTS audit_log (
    audit_number BIGSERIAL PRIMARY KEY,
    action_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    login VARCHAR(50) REFERENCES users(login) ON DELETE SET NULL,
    operation VARCHAR(20) NOT NULL,
    table_name VARCHAR(40) NOT NULL,
    details TEXT
);

-- =====================================================================
-- Индексы
-- =====================================================================
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role_name);
CREATE INDEX IF NOT EXISTS idx_students_curator ON students(curator_personnel_number);
CREATE INDEX IF NOT EXISTS idx_protocols_student ON test_protocols(record_book_number);
CREATE INDEX IF NOT EXISTS idx_protocols_test ON test_protocols(test_code);
CREATE INDEX IF NOT EXISTS idx_protocols_taken_at ON test_protocols(taken_at);
CREATE INDEX IF NOT EXISTS idx_questions_test ON questions(test_code);
CREATE INDEX IF NOT EXISTS idx_answers_question ON answers(question_number);
CREATE INDEX IF NOT EXISTS idx_item_results_protocol ON item_results(protocol_number);
CREATE INDEX IF NOT EXISTS idx_recommendations_grade ON recommendations(grade_name);
CREATE INDEX IF NOT EXISTS idx_tests_indicator ON tests(indicator_name);
CREATE INDEX IF NOT EXISTS idx_consultations_student ON consultations(record_book_number);
CREATE INDEX IF NOT EXISTS idx_consultations_psy ON consultations(personnel_number);
CREATE INDEX IF NOT EXISTS idx_notes_protocol ON test_notes(protocol_number);
CREATE INDEX IF NOT EXISTS idx_notes_psychologist ON test_notes(personnel_number);
CREATE INDEX IF NOT EXISTS idx_audit_login ON audit_log(login);
CREATE INDEX IF NOT EXISTS idx_audit_table ON audit_log(table_name);

-- =====================================================================
-- Доп. ограничения целостности (UNIQUE)
-- =====================================================================
CREATE UNIQUE INDEX IF NOT EXISTS uq_question_test_order
    ON questions(test_code, order_number);
CREATE UNIQUE INDEX IF NOT EXISTS uq_answer_question_order
    ON answers(question_number, order_number);
CREATE UNIQUE INDEX IF NOT EXISTS uq_tests_name ON tests(name);
CREATE UNIQUE INDEX IF NOT EXISTS uq_recommendations_grade_text
    ON recommendations(grade_name, recommendation_text);

-- =====================================================================
-- Представление: динамика результатов студентов
-- =====================================================================
CREATE OR REPLACE VIEW v_student_dynamics AS
SELECT
    s.record_book_number,
    s.last_name || ' ' || s.first_name AS full_name,
    s.group_name,
    p.test_code,
    t.name AS test_name,
    p.taken_at,
    p.total_score,
    p.grade_name
FROM test_protocols p
JOIN students s ON s.record_book_number = p.record_book_number
JOIN tests t ON t.test_code = p.test_code
ORDER BY s.record_book_number, p.taken_at;

-- =====================================================================
-- ВНИМАНИЕ: триггеры и хранимые процедуры (PL/pgSQL) создаются
-- отдельно через Spring-компонент DbProceduresInitializer.
-- =====================================================================
