-- =====================================================================
-- Схема БД «Мониторинг ментального здоровья» (PostgreSQL).
-- Все первичные ключи — естественные (см. ПЗ, разделы 2.1.2 и 2.2.2).
-- Единственная junction-таблица с составным PK — psychologist_specializations.
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

-- 3. Студенты
CREATE TABLE IF NOT EXISTS students (
    record_book_number BIGINT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE REFERENCES users(login) ON DELETE CASCADE,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    group_name VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20)
);

-- 4. Психологи
CREATE TABLE IF NOT EXISTS psychologists (
    personnel_number BIGINT PRIMARY KEY,
    login VARCHAR(50) NOT NULL UNIQUE REFERENCES users(login) ON DELETE CASCADE,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    middle_name VARCHAR(50),
    position VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20)
);

-- 5. Категории тестов
CREATE TABLE IF NOT EXISTS test_categories (
    category_name VARCHAR(50) PRIMARY KEY,
    description TEXT
);

-- 6. Статусы прохождения теста
CREATE TABLE IF NOT EXISTS result_statuses (
    status_name VARCHAR(30) PRIMARY KEY,
    description TEXT
);

-- 7. Уровни стресса
CREATE TABLE IF NOT EXISTS stress_levels (
    level_name VARCHAR(30) PRIMARY KEY,
    min_percent SMALLINT NOT NULL,
    max_percent SMALLINT NOT NULL,
    CHECK (min_percent >= 0 AND min_percent <= 100),
    CHECK (max_percent >= 0 AND max_percent <= 100),
    CHECK (min_percent <= max_percent)
);

-- 8. Тесты
CREATE TABLE IF NOT EXISTS tests (
    test_code VARCHAR(20) PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    instructions TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    author_personnel_number BIGINT REFERENCES psychologists(personnel_number),
    category_name VARCHAR(50) REFERENCES test_categories(category_name)
);

-- 9. Вопросы
CREATE TABLE IF NOT EXISTS questions (
    question_number BIGSERIAL PRIMARY KEY,
    test_code VARCHAR(20) NOT NULL REFERENCES tests(test_code) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    order_number SMALLINT NOT NULL
);

-- 10. Варианты ответов
CREATE TABLE IF NOT EXISTS answers (
    answer_code BIGSERIAL PRIMARY KEY,
    question_number BIGINT NOT NULL REFERENCES questions(question_number) ON DELETE CASCADE,
    answer_text VARCHAR(200) NOT NULL,
    score SMALLINT NOT NULL CHECK (score >= 0),
    order_number SMALLINT NOT NULL
);

-- 11. Протоколы тестирования
CREATE TABLE IF NOT EXISTS test_protocols (
    protocol_number BIGSERIAL PRIMARY KEY,
    record_book_number BIGINT NOT NULL REFERENCES students(record_book_number) ON DELETE CASCADE,
    test_code VARCHAR(20) NOT NULL REFERENCES tests(test_code),
    taken_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_score SMALLINT NOT NULL CHECK (total_score >= 0),
    level_name VARCHAR(30) REFERENCES stress_levels(level_name),
    status_name VARCHAR(30) REFERENCES result_statuses(status_name)
);

-- 12. Ответы пользователя
CREATE TABLE IF NOT EXISTS result_answers (
    response_number BIGSERIAL PRIMARY KEY,
    protocol_number BIGINT NOT NULL REFERENCES test_protocols(protocol_number) ON DELETE CASCADE,
    question_number BIGINT NOT NULL REFERENCES questions(question_number),
    answer_code BIGINT NOT NULL REFERENCES answers(answer_code)
);

-- 13. Рекомендации
CREATE TABLE IF NOT EXISTS recommendations (
    recommendation_code BIGSERIAL PRIMARY KEY,
    level_name VARCHAR(30) NOT NULL REFERENCES stress_levels(level_name),
    recommendation_text TEXT NOT NULL,
    order_number SMALLINT,
    author_personnel_number BIGINT REFERENCES psychologists(personnel_number)
);

-- 14. Специализации психологов (junction M:N)
CREATE TABLE IF NOT EXISTS psychologist_specializations (
    personnel_number BIGINT NOT NULL REFERENCES psychologists(personnel_number) ON DELETE CASCADE,
    category_name VARCHAR(50) NOT NULL REFERENCES test_categories(category_name) ON DELETE CASCADE,
    PRIMARY KEY (personnel_number, category_name)
);

-- 15. Заметки психолога
CREATE TABLE IF NOT EXISTS psychologist_notes (
    note_number BIGSERIAL PRIMARY KEY,
    personnel_number BIGINT NOT NULL REFERENCES psychologists(personnel_number) ON DELETE CASCADE,
    record_book_number BIGINT NOT NULL REFERENCES students(record_book_number) ON DELETE CASCADE,
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
CREATE INDEX IF NOT EXISTS idx_protocols_student ON test_protocols(record_book_number);
CREATE INDEX IF NOT EXISTS idx_protocols_test ON test_protocols(test_code);
CREATE INDEX IF NOT EXISTS idx_protocols_taken_at ON test_protocols(taken_at);
CREATE INDEX IF NOT EXISTS idx_questions_test ON questions(test_code);
CREATE INDEX IF NOT EXISTS idx_answers_question ON answers(question_number);
CREATE INDEX IF NOT EXISTS idx_result_answers_protocol ON result_answers(protocol_number);
CREATE INDEX IF NOT EXISTS idx_recommendations_level ON recommendations(level_name);
CREATE INDEX IF NOT EXISTS idx_notes_student ON psychologist_notes(record_book_number);
CREATE INDEX IF NOT EXISTS idx_notes_psychologist ON psychologist_notes(personnel_number);
CREATE INDEX IF NOT EXISTS idx_audit_login ON audit_log(login);
CREATE INDEX IF NOT EXISTS idx_audit_table ON audit_log(table_name);

-- =====================================================================
-- Доп. ограничения целостности
-- =====================================================================
CREATE UNIQUE INDEX IF NOT EXISTS uq_question_test_order
    ON questions(test_code, order_number);

CREATE UNIQUE INDEX IF NOT EXISTS uq_answer_question_order
    ON answers(question_number, order_number);

CREATE UNIQUE INDEX IF NOT EXISTS uq_protocol_question
    ON result_answers(protocol_number, question_number);

CREATE UNIQUE INDEX IF NOT EXISTS uq_tests_name ON tests(name);
CREATE UNIQUE INDEX IF NOT EXISTS uq_recommendations_level_text
    ON recommendations(level_name, recommendation_text);

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
    p.level_name,
    p.status_name
FROM test_protocols p
JOIN students s ON s.record_book_number = p.record_book_number
JOIN tests t ON t.test_code = p.test_code
ORDER BY s.record_book_number, p.taken_at;

-- =====================================================================
-- ВНИМАНИЕ: триггеры и хранимые процедуры (PL/pgSQL) создаются
-- отдельно через Spring-компонент DbProceduresInitializer.
-- =====================================================================
