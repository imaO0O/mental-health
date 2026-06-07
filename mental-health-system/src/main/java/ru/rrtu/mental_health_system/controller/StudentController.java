package ru.rrtu.mental_health_system.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.rrtu.mental_health_system.model.*;
import ru.rrtu.mental_health_system.repository.StudentRepository;
import ru.rrtu.mental_health_system.service.TestService;
import ru.rrtu.mental_health_system.service.UserService;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);

    private final UserService userService;
    private final TestService testService;
    private final StudentRepository studentRepository;

    public StudentController(UserService userService, TestService testService,
                             StudentRepository studentRepository) {
        this.userService = userService;
        this.testService = testService;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Student student = studentRepository.findByLogin(user.getLogin());
        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("tests", testService.findAll());
        return "student/dashboard";
    }

    @GetMapping("/test/{code}")
    public String takeTest(@PathVariable("code") String testCode, Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Test test = testService.findById(testCode).orElse(null);
        if (test == null) return "redirect:/student/dashboard";

        List<Question> questions = testService.getQuestionsByTestId(testCode);
        Map<Long, List<Answer>> questionAnswersMap = new java.util.HashMap<>();
        for (Question question : questions) {
            questionAnswersMap.put(question.getQuestionNumber(),
                    testService.getAnswersByQuestionId(question.getQuestionNumber()));
        }

        model.addAttribute("test", test);
        model.addAttribute("questions", questions);
        model.addAttribute("questionAnswersMap", questionAnswersMap);
        return "student/take-test";
    }

    @PostMapping("/test/{code}/submit")
    public String submitTest(@PathVariable("code") String testCode,
                             @RequestParam Map<String, String> answers,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Test test = testService.findById(testCode).orElse(null);
        if (test == null) return "redirect:/student/dashboard";

        short totalScore = 0;
        List<Question> questions = testService.getQuestionsByTestId(testCode);

        for (Question question : questions) {
            String answerId = answers.get("question_" + question.getQuestionNumber());
            if (answerId != null) {
                try {
                    Long ansCode = Long.parseLong(answerId);
                    List<Answer> qAnswers = testService.getAnswersByQuestionId(question.getQuestionNumber());
                    for (Answer a : qAnswers) {
                        if (a.getAnswerCode().equals(ansCode) && a.getScore() != null) {
                            totalScore += a.getScore();
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    // игнорируем
                }
            }
        }

        int maxPossible = testService.getMaxPossibleScore(testCode);
        Grade stressLevel = testService.getStressLevelForResult(totalScore, maxPossible);

        Student student = studentRepository.findByLogin(user.getLogin());
        if (student == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Студент не найден. Ваш логин: " + user.getLogin());
            return "redirect:/student/dashboard";
        }

        try {
            TestResult result = testService.saveTestResult(student, test, totalScore, stressLevel);
            redirectAttributes.addFlashAttribute("success",
                    "Тест успешно пройден! Балл: " + totalScore);
            return "redirect:/student/result/" + result.getProtocolNumber();
        } catch (Exception e) {
            log.error("Ошибка при сохранении протокола: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при сохранении протокола: " + e.getMessage());
            return "redirect:/student/test/" + testCode;
        }
    }

    @GetMapping("/results")
    public String results(@RequestParam(required = false) String levelName,
                          @RequestParam(required = false) String from,
                          @RequestParam(required = false) String to,
                          Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Student student = studentRepository.findByLogin(user.getLogin());

        if (student == null) {
            model.addAttribute("results", List.of());
            model.addAttribute("warning", "Профиль студента не найден. Обратитесь к администратору.");
        } else {
            List<TestResult> all = testService.getResultsByStudentId(student.getRecordBookNumber());
            java.time.LocalDate fromDate = parseDate(from);
            java.time.LocalDate toDate = parseDate(to);
            List<TestResult> filtered = all.stream()
                    .filter(r -> levelName == null || levelName.isBlank()
                            || (r.getStressLevel() != null
                                && levelName.equals(r.getStressLevel().getLevelName())))
                    .filter(r -> fromDate == null
                            || (r.getDateTaken() != null && !r.getDateTaken().isBefore(fromDate)))
                    .filter(r -> toDate == null
                            || (r.getDateTaken() != null && !r.getDateTaken().isAfter(toDate)))
                    .toList();
            model.addAttribute("results", filtered);
            model.addAttribute("totalCount", all.size());
        }

        model.addAttribute("user", user);
        model.addAttribute("levels", testService.getAllStressLevels());
        model.addAttribute("filterLevel", levelName);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "student/results";
    }

    private java.time.LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return java.time.LocalDate.parse(s); }
        catch (Exception e) { return null; }
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Student student = studentRepository.findByLogin(user.getLogin());
        model.addAttribute("user", user);
        model.addAttribute("student", student);
        return "student/profile";
    }

    @GetMapping("/result/{n}")
    public String resultDetail(@PathVariable("n") Long protocolNumber, Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Student student = studentRepository.findByLogin(user.getLogin());
        if (student == null) return "redirect:/student/results";

        TestResult result = testService.findResultById(protocolNumber).orElse(null);
        if (result == null || result.getStudent() == null
                || !result.getStudent().getRecordBookNumber().equals(student.getRecordBookNumber())) {
            return "redirect:/student/results";
        }

        List<Recommendation> recs = result.getStressLevel() != null
                ? testService.getRecommendationsByStressLevelId(result.getStressLevel().getLevelName())
                : List.of();

        int maxScore = result.getTest() != null
                ? testService.getMaxPossibleScore(result.getTest().getTestCode())
                : 0;
        int percent = maxScore > 0
                ? Math.round((result.getTotalScore() * 100f) / maxScore)
                : 0;

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("result", result);
        model.addAttribute("test", result.getTest());
        model.addAttribute("score", result.getTotalScore());
        model.addAttribute("maxScore", maxScore);
        model.addAttribute("percent", percent);
        model.addAttribute("stressLevel", result.getStressLevel());
        model.addAttribute("recommendations", recs);
        return "student/test-result";
    }

    @GetMapping("/recommendations")
    public String recommendations(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Student student = studentRepository.findByLogin(user.getLogin());
        List<TestResult> history = student == null ? List.of()
                : testService.getResultsByStudentId(student.getRecordBookNumber());

        TestResult last = history.stream()
                .max((a, b) -> {
                    int byDate = a.getTakenAt().compareTo(b.getTakenAt());
                    if (byDate != 0) return byDate;
                    return Long.compare(a.getProtocolNumber(), b.getProtocolNumber());
                })
                .orElse(null);
        List<Recommendation> recs = (last != null && last.getStressLevel() != null)
                ? testService.getRecommendationsByStressLevelId(last.getStressLevel().getLevelName())
                : testService.getAllRecommendations();

        model.addAttribute("user", user);
        model.addAttribute("lastResult", last);
        model.addAttribute("recommendations", recs);
        return "student/recommendations";
    }
}
