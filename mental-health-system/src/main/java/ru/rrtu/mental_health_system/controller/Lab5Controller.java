package ru.rrtu.mental_health_system.controller;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rrtu.mental_health_system.model.*;
import ru.rrtu.mental_health_system.repository.*;
import ru.rrtu.mental_health_system.service.TestService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Лабораторная работа №5 — навигационный подход (без SQL-запросов).
 *
 * В Java/JPA "навигационный подход" реализуется через репозитории и обход
 * связей сущностей (`getStudent()`, `getTest()` и т.п.) без написания SQL.
 *
 * В пункте 5 допустим JOIN-запрос для исходного набора данных, но
 * вычисляемое поле формируется на стороне Java.
 *
 * Тематика — мониторинг ментального здоровья:
 *   "товары" → тесты, "поставки" → результаты тестов.
 */
@Controller
@RequestMapping("/lab5")
public class Lab5Controller {

    private final TestRepository testRepository;
    private final TestResultRepository testResultRepository;
    private final TestService testService;

    public Lab5Controller(TestRepository testRepository,
                          TestResultRepository testResultRepository,
                          TestService testService) {
        this.testRepository = testRepository;
        this.testResultRepository = testResultRepository;
        this.testService = testService;
    }

    @GetMapping
    public String index(@RequestParam(name = "tab", defaultValue = "1") String tab,
                        Model model) {
        model.addAttribute("tab", tab);
        model.addAttribute("tests", testRepository.findAll());
        model.addAttribute("results", testResultRepository.findAll());
        model.addAttribute("avgScore", currentAverage());

        // Для пункта 5 — список с вычисляемым полем "процент"
        model.addAttribute("resultRows", buildResultRows());
        return "lab5/index";
    }

    // ───────── ЛР5/1: добавить новый тест (без SQL — через JpaRepository.save) ─────────
    @PostMapping("/task1")
    public String task1(@RequestParam String testCode,
                        @RequestParam String name,
                        @RequestParam(required = false) String description,
                        @RequestParam(required = false) String instructions,
                        RedirectAttributes ra) {
        Test t = new Test();
        t.setTestCode(testCode);
        t.setName(name);
        t.setDescription(description);
        t.setInstructions(instructions);
        t.setIsActive(true);
        try {
            testRepository.save(t);
            ra.addFlashAttribute("msg",
                    "Тест «" + name + "» (шифр " + testCode + ") добавлен через testRepository.save() (без SQL).");
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err", "Ошибка: " + e.getMostSpecificCause().getMessage());
        }
        return "redirect:/lab5?tab=1";
    }

    // ───────── ЛР5/2: ступенчатая корректировка баллов всех результатов ─────────
    // Аналог "сезонных скидок": в зависимости от значения total_score применяется
    // разный коэффициент снижения. Все изменения — через навигацию по сущностям
    // и save(); нативного SQL UPDATE нет.
    @PostMapping("/task2")
    @Transactional
    public String task2(RedirectAttributes ra) {
        List<TestResult> all = testResultRepository.findAll();
        int changed = 0;
        for (TestResult r : all) {
            if (r.getTotalScore() == null) continue;
            int s = r.getTotalScore();
            double k;
            if (s < 10) k = 0.90;            // -10%
            else if (s <= 20) k = 0.80;      // -20%
            else k = 0.70;                   // -30%
            int newScore = (int) Math.round(s * k);
            r.setTotalScore((short) newScore);
            // Пересчитываем уровень стресса навигационно
            recalcStressLevel(r);
            testResultRepository.save(r);
            changed++;
        }
        ra.addFlashAttribute("msg",
                "Применена ступенчатая корректировка к " + changed + " результатам "
              + "(ставки: <10 → -10%; 10..20 → -20%; >20 → -30%).");
        return "redirect:/lab5?tab=2";
    }

    // ───────── ЛР5/3: удалить заданный тест (по шифру) — через repository.delete ─────────
    @PostMapping("/task3")
    @Transactional
    public String task3(@RequestParam("testId") String testCode, RedirectAttributes ra) {
        try {
            Test t = testRepository.findById(testCode).orElse(null);
            if (t == null) {
                ra.addFlashAttribute("err", "Тест с шифром «" + testCode + "» не найден.");
            } else {
                String name = t.getName();
                testRepository.delete(t);
                ra.addFlashAttribute("msg", "Тест «" + name + "» удалён через testRepository.delete().");
            }
        } catch (DataAccessException e) {
            ra.addFlashAttribute("err",
                    "Ошибка удаления: " + e.getMostSpecificCause().getMessage()
                  + " (возможно, по тесту есть результаты — срабатывает триггер "
                  + "tests_protect_delete).");
        }
        return "redirect:/lab5?tab=3";
    }

    // ───────── ЛР5/4: цикл, пока среднее >= порога ─────────
    // Уменьшаем total_score у всех результатов на 10% в цикле, пока среднее
    // не станет меньше заданного порога. Все операции через JPA, без SQL.
    @PostMapping("/task4")
    @Transactional
    public String task4(@RequestParam(defaultValue = "5") double threshold,
                        @RequestParam(defaultValue = "50") int maxIters,
                        RedirectAttributes ra) {
        int iter = 0;
        double avg = currentAverage();
        double startAvg = avg;
        while (avg >= threshold && iter < maxIters) {
            List<TestResult> all = testResultRepository.findAll();
            if (all.isEmpty()) break;
            for (TestResult r : all) {
                if (r.getTotalScore() == null) continue;
                int newScore = (int) Math.round(r.getTotalScore() * 0.9);
                r.setTotalScore((short) newScore);
                recalcStressLevel(r);
            }
            testResultRepository.saveAll(all);
            avg = currentAverage();
            iter++;
        }
        ra.addFlashAttribute("msg", String.format(
                "Стартовое среднее: %.2f → конечное: %.2f. Выполнено итераций: %d "
              + "(порог %.2f%s).", startAvg, avg, iter, threshold,
                iter >= maxIters ? ", остановлено по лимиту" : ""));
        return "redirect:/lab5?tab=4";
    }

    // ───────── helpers ─────────

    private double currentAverage() {
        List<TestResult> all = testResultRepository.findAll();
        if (all.isEmpty()) return 0.0;
        double sum = 0;
        int n = 0;
        for (TestResult r : all) {
            if (r.getTotalScore() != null) { sum += r.getTotalScore(); n++; }
        }
        return n == 0 ? 0.0 : sum / n;
    }

    private void recalcStressLevel(TestResult r) {
        if (r.getTest() == null) return;
        int max = testService.getMaxPossibleScore(r.getTest().getTestCode());
        Short score = r.getTotalScore() == null ? (short) 0 : r.getTotalScore();
        StressLevel newLevel = testService.getStressLevelForResult(score, max);
        if (newLevel != null) {
            r.setStressLevel(newLevel);
        }
    }

    /**
     * ЛР5/5. Для исходного набора данных используем JOIN (через JPA, всё ещё без
     * рукописного SQL: testResultRepository.findAll() + навигация по связям),
     * а вычисляемое поле «процент от максимального балла» формируем в Java.
     */
    private List<Map<String, Object>> buildResultRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (TestResult r : testResultRepository.findAll()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("studentName",
                    r.getStudent() == null ? "—" : r.getStudent().getFullName());
            m.put("recordBookNumber",
                    r.getStudent() == null ? "—" : r.getStudent().getRecordBookNumber());
            m.put("testName", r.getTest() == null ? "—" : r.getTest().getName());
            int score = r.getTotalScore() == null ? 0 : r.getTotalScore();
            int max = r.getTest() == null ? 0
                    : testService.getMaxPossibleScore(r.getTest().getTestCode());
            // ВЫЧИСЛЯЕМОЕ ПОЛЕ — формируется в клиентском приложении (в Java)
            double percent = max > 0 ? (score * 100.0 / max) : 0.0;
            m.put("score", score);
            m.put("maxScore", max);
            m.put("percent", String.format("%.1f", percent));
            m.put("stressLevel",
                    r.getStressLevel() == null ? "—" : r.getStressLevel().getName());
            m.put("dateTaken", r.getDateTaken());
            rows.add(m);
        }
        return rows;
    }
}
