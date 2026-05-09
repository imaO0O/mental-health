package ru.rrtu.mental_health_system.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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

    // ───────── ЛР3/5: суммарное количество прохождений теста ─────────
    @PostMapping("/task5")
    public String task5(@RequestParam String testCode, RedirectAttributes ra) {
        try {
            Long total = jdbc.execute((java.sql.Connection con) -> {
                CallableStatement cs = con.prepareCall("{call sp_lab3_test_total(?, ?)}");
                cs.setString(1, testCode);
                cs.registerOutParameter(2, Types.BIGINT);
                cs.execute();
                return cs.getLong(2);
            });
            ra.addFlashAttribute("msg",
                    "По тесту «" + testCode + "» зафиксировано прохождений: " + total);
            ra.addFlashAttribute("totalCount", total);
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
            // Создаём пользователя-заглушку, чтобы соблюсти FK psychologists.user_id
            String login = "psy_" + (System.currentTimeMillis() % 100000000L);
            Long userId = jdbc.queryForObject("""
                INSERT INTO users(login, password_hash, role_id)
                SELECT ?, ?, role_id FROM roles WHERE name='PSYCHOLOGIST'
                RETURNING user_id
                """, Long.class, login, "{noop}stub");
            jdbc.update("""
                INSERT INTO psychologists(personnel_number, user_id, last_name, first_name, middle_name,
                                          position, email, phone)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, personnelNumber, userId, lastName, firstName, middleName, position, email, phone);
            ra.addFlashAttribute("msg", "Психолог добавлен (таб. №=" + personnelNumber + ", login=" + login + ").");
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
            // Удаляем психолога; user_id привязан CASCADE — удалим и пользователя.
            Long userId = jdbc.queryForObject(
                    "SELECT user_id FROM psychologists WHERE personnel_number = ?",
                    Long.class, personnelNumber);
            jdbc.update("DELETE FROM psychologists WHERE personnel_number = ?", personnelNumber);
            if (userId != null) {
                jdbc.update("DELETE FROM users WHERE user_id = ?", userId);
            }
            ra.addFlashAttribute("msg", "Психолог таб. №=" + personnelNumber + " удалён.");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab3?tab=6";
    }

    // ───────── вспомогательные SELECT'ы ─────────

    private List<Map<String, Object>> fetchTests() {
        return jdbc.queryForList("""
            SELECT test_id, name, description, is_active
              FROM tests ORDER BY test_id
            """);
    }

    private List<Map<String, Object>> fetchResults() {
        return jdbc.queryForList("""
            SELECT r.result_id, u.login AS student_login,
                   t.name AS test_name, r.total_score, r.date_taken,
                   sl.level_name AS stress_level
              FROM test_results r
              JOIN students s ON s.record_book_number = r.student_id
              JOIN users u ON u.user_id = s.user_id
              JOIN tests t ON t.test_id = r.test_id
              JOIN stress_levels sl ON sl.level_id = r.level_id
             ORDER BY r.result_id
            """);
    }

    private List<Map<String, Object>> fetchPsychologists() {
        // PK теперь = personnel_number (числовой), отдельного psychologist_id нет.
        // Алиас "id" сохраняется для шаблона (в т.ч. имена скрытых форм upd-/del-).
        return jdbc.queryForList("""
            SELECT personnel_number AS id, last_name, first_name, middle_name,
                   position, personnel_number, email, phone
              FROM psychologists ORDER BY personnel_number
            """);
    }
}
