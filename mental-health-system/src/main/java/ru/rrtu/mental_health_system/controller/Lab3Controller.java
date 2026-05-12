package ru.rrtu.mental_health_system.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.CallableStatement;
import java.sql.Types;
import java.util.List;
import java.util.Map;

/**
 * Лабораторная работа №3 — вызов хранимых процедур из Java/Spring
 * через JdbcTemplate (аналог SqlCommand из C#).
 *
 * Тематика адаптирована под мониторинг ментального здоровья:
 *   "товары"      → "тесты" (таблица tests)
 *   "поставки"    → "результаты тестов" (test_results)
 *   "поставщики"  → "психологи" (psychologists)
 */
@Controller
@RequestMapping("/lab3")
public class Lab3Controller {

    private final JdbcTemplate jdbc;

    public Lab3Controller(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public String index(@RequestParam(name = "tab", defaultValue = "1") String tab,
                        Model model) {
        model.addAttribute("tab", tab);

        // Снимки таблиц для каждой вкладки (выводится в DataGridView-подобную таблицу)
        model.addAttribute("results", fetchResults());
        model.addAttribute("tests", fetchTests());
        model.addAttribute("psychologists", fetchPsychologists());

        // Списки для datalist-подсказок: чтобы пользователь не вводил
        // коды тестов и логины студентов наугад.
        model.addAttribute("testCodes", fetchTestCodes());
        model.addAttribute("studentLogins", fetchStudentLogins());
        return "lab3/index";
    }

    // ───────── ЛР3/1: уменьшить total_score у всех результатов на N% ─────────
    @PostMapping("/task1")
    public String task1(@RequestParam(defaultValue = "20") int percent,
                        RedirectAttributes ra) {
        ra.addFlashAttribute("beforeResults", fetchResults());
        try {
            jdbc.update("CALL sp_lab3_decrease_scores(?)", percent);
            ra.addFlashAttribute("msg",
                    "Процедура sp_lab3_decrease_scores(" + percent + ") выполнена. "
                  + "Баллы всех результатов уменьшены на " + percent + "%.");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab3?tab=1";
    }

    // ───────── ЛР3/2: добавить новый психологический тест ─────────
    @PostMapping("/task2")
    public String task2(@RequestParam String testCode,
                        @RequestParam String name,
                        @RequestParam(defaultValue = "") String description,
                        @RequestParam(defaultValue = "") String instructions,
                        RedirectAttributes ra) {
        ra.addFlashAttribute("beforeTests", fetchTests());
        try {
            jdbc.update("CALL sp_lab3_add_test(?, ?, ?, ?)", testCode, name, description, instructions);
            ra.addFlashAttribute("msg", "Тест «" + name + "» (шифр " + testCode + ") добавлен через sp_lab3_add_test.");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab3?tab=2";
    }

    // ───────── ЛР3/3: удалить протоколы студента по шифру теста ─────────
    @PostMapping("/task3")
    public String task3(@RequestParam String studentLogin,
                        @RequestParam String testCode,
                        RedirectAttributes ra) {
        ra.addFlashAttribute("beforeResults", fetchResults());
        try {
            jdbc.update("CALL sp_lab3_delete_protocols(?, ?)", studentLogin, testCode);
            ra.addFlashAttribute("msg",
                    "Протоколы студента «" + studentLogin + "» по тесту «" + testCode
                  + "» удалены через sp_lab3_delete_protocols.");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab3?tab=3";
    }

    // ───────── ЛР3/4: удалить тест с возвратом кода ошибки ─────────
    @PostMapping("/task4")
    public String task4(@RequestParam String testCode, RedirectAttributes ra) {
        ra.addFlashAttribute("beforeTests", fetchTests());
        try {
            Integer code = jdbc.queryForObject(
                    "SELECT fn_lab3_delete_test(?)", Integer.class, testCode);
            String text = switch (code == null ? -1 : code) {
                case 0 -> "Код 0 — тест «" + testCode + "» успешно удалён.";
                case 1 -> "Код 1 — удаление невозможно: по тесту существуют протоколы тестирования.";
                case 2 -> "Код 2 — теста с таким шифром нет в БД.";
                default -> "Неизвестный код: " + code;
            };
            ra.addFlashAttribute("msg", text);
            ra.addFlashAttribute("returnCode", code);
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab3?tab=4";
    }

    // ───────── ЛР3/5: количество прохождений теста (опционально — по студенту) ─────────
    // Процедура принимает 2 IN-параметра и 1 OUT-параметр:
    //   p_test_code      — шифр теста (обязательно);
    //   p_student_login  — логин студента (можно пустой → считаем по всем);
    //   p_total          — OUT BIGINT: количество или код ошибки (-1, -2).
    // Используем прямой "CALL ...": JDBC-escape "{call ...}" в pgjdbc
    // транслируется в "SELECT * FROM ...", что для PROCEDURE недопустимо.
    @PostMapping("/task5")
    public String task5(@RequestParam String testCode,
                        @RequestParam(required = false, defaultValue = "") String studentLogin,
                        RedirectAttributes ra) {
        final String login = studentLogin == null ? "" : studentLogin.trim();
        try {
            Long total = jdbc.execute((java.sql.Connection con) -> {
                try (CallableStatement cs = con.prepareCall("CALL sp_lab3_test_total(?, ?, ?)")) {
                    cs.setString(1, testCode);
                    cs.setString(2, login);
                    cs.registerOutParameter(3, Types.BIGINT);
                    cs.execute();
                    return cs.getLong(3);
                }
            });
            if (total != null && total == -1) {
                ra.addFlashAttribute("err",
                        "Тест с шифром «" + testCode + "» не найден в БД.");
            } else if (total != null && total == -2) {
                ra.addFlashAttribute("err",
                        "Студент с логином «" + login + "» не найден в БД.");
            } else {
                String scope = login.isEmpty()
                        ? "По тесту «" + testCode + "» всего прохождений: "
                        : "Студент «" + login + "» прошёл тест «" + testCode + "» раз: ";
                ra.addFlashAttribute("msg", scope + total);
                ra.addFlashAttribute("totalCount", total);
                ra.addFlashAttribute("scopeLabel", login.isEmpty() ? "все студенты" : login);
            }
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab3?tab=5";
    }

    // ───────── ЛР3/6: CRUD по таблице psychologists (аналог SqlDataAdapter) ─────────
    @PostMapping("/task6/insert")
    public String task6Insert(@RequestParam Long personnelNumber,
                              @RequestParam String lastName,
                              @RequestParam String firstName,
                              @RequestParam(required = false) String middleName,
                              @RequestParam(required = false) String position,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String phone,
                              RedirectAttributes ra) {
        ra.addFlashAttribute("beforePsy", fetchPsychologists());
        try {
            // Проверка дублей до вставки — даём пользователю понятную ошибку
            // вместо «duplicate key value violates unique constraint».
            Integer exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM psychologists WHERE personnel_number = ?",
                    Integer.class, personnelNumber);
            if (exists != null && exists > 0) {
                ra.addFlashAttribute("err",
                        "Психолог с табельным № " + personnelNumber + " уже существует.");
                return "redirect:/lab3?tab=6";
            }
            // Создаём учётную запись (PK = login), затем психолога с FK login.
            // Пароль кодируем через BCrypt — иначе вход в систему не сработает.
            String login = "psy_" + personnelNumber;
            String passwordHash = new BCryptPasswordEncoder().encode("psychologist123");
            jdbc.update("""
                INSERT INTO users(login, password_hash, role_name)
                VALUES (?, ?, 'PSYCHOLOGIST')
                """, login, passwordHash);
            jdbc.update("""
                INSERT INTO psychologists(personnel_number, login, last_name, first_name, middle_name,
                                          position, email, phone)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, personnelNumber, login, lastName, firstName, middleName, position, email, phone);
            ra.addFlashAttribute("msg",
                    "Психолог добавлен: табельный № " + personnelNumber
                  + ", логин «" + login + "», пароль psychologist123.");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab3?tab=6";
    }

    @PostMapping("/task6/update")
    public String task6Update(@RequestParam("id") Long personnelNumber,
                              @RequestParam String lastName,
                              @RequestParam String firstName,
                              @RequestParam(required = false) String middleName,
                              @RequestParam(required = false) String position,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String phone,
                              RedirectAttributes ra) {
        ra.addFlashAttribute("beforePsy", fetchPsychologists());
        try {
            jdbc.update("CALL sp_lab3_psychologist_update(?, ?, ?, ?, ?, ?, ?)",
                    personnelNumber, lastName, firstName, middleName, position, email, phone);
            ra.addFlashAttribute("msg",
                    "Психолог таб. №=" + personnelNumber + " обновлён.");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab3?tab=6";
    }

    @PostMapping("/task6/delete")
    public String task6Delete(@RequestParam("id") Long personnelNumber, RedirectAttributes ra) {
        ra.addFlashAttribute("beforePsy", fetchPsychologists());
        try {
            // Получаем логин (FK), удаляем психолога, затем учётную запись.
            String login = jdbc.queryForObject(
                    "SELECT login FROM psychologists WHERE personnel_number = ?",
                    String.class, personnelNumber);
            jdbc.update("DELETE FROM psychologists WHERE personnel_number = ?", personnelNumber);
            if (login != null) {
                jdbc.update("DELETE FROM users WHERE login = ?", login);
            }
            ra.addFlashAttribute("msg", "Психолог таб. №=" + personnelNumber + " удалён.");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab3?tab=6";
    }

    // ───────── вспомогательные SELECT'ы ─────────
    // Все обращения — к новой схеме БД (естественные ключи, см. ПЗ).

    private List<Map<String, Object>> fetchTests() {
        // Алиас "test_id" сохраняем, чтобы шаблон lab3/index.html
        // не пришлось менять, но возвращаем шифр теста (естественный PK).
        return jdbc.queryForList("""
            SELECT test_code AS test_id, name, description, is_active
              FROM tests ORDER BY test_code
            """);
    }

    private List<Map<String, Object>> fetchResults() {
        // Таблица test_results теперь test_protocols, FK по логину/шифру/уровню.
        // Аналогично — отдаём алиасы старых имён для совместимости с шаблоном.
        return jdbc.queryForList("""
            SELECT p.protocol_number AS result_id,
                   u.login AS student_login,
                   t.name AS test_name,
                   p.total_score,
                   p.taken_at AS date_taken,
                   p.level_name AS stress_level
              FROM test_protocols p
              JOIN students s ON s.record_book_number = p.record_book_number
              JOIN users u ON u.login = s.login
              JOIN tests t ON t.test_code = p.test_code
             ORDER BY p.protocol_number
            """);
    }

    private List<Map<String, Object>> fetchPsychologists() {
        // PK = personnel_number; алиас "id" нужен шаблону (скрытые поля
        // в формах upd-/del-).
        return jdbc.queryForList("""
            SELECT personnel_number AS id, last_name, first_name, middle_name,
                   position, personnel_number, email, phone
              FROM psychologists ORDER BY personnel_number
            """);
    }

    private List<String> fetchTestCodes() {
        return jdbc.queryForList("SELECT test_code FROM tests ORDER BY test_code", String.class);
    }

    private List<String> fetchStudentLogins() {
        return jdbc.queryForList(
                "SELECT login FROM students ORDER BY login", String.class);
    }
}
