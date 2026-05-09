package ru.rrtu.mental_health_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.rrtu.mental_health_system.model.*;
import ru.rrtu.mental_health_system.repository.RecommendationRepository;
import ru.rrtu.mental_health_system.repository.StressLevelRepository;
import ru.rrtu.mental_health_system.repository.StudentRepository;
import ru.rrtu.mental_health_system.service.TestService;
import ru.rrtu.mental_health_system.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/psychologist")
public class PsychologistController {

    private final UserService userService;
    private final TestService testService;
    private final StudentRepository studentRepository;
    private final RecommendationRepository recommendationRepository;
    private final StressLevelRepository stressLevelRepository;

    public PsychologistController(UserService userService,
                                  TestService testService,
                                  StudentRepository studentRepository,
                                  RecommendationRepository recommendationRepository,
                                  StressLevelRepository stressLevelRepository) {
        this.userService = userService;
        this.testService = testService;
        this.studentRepository = studentRepository;
        this.recommendationRepository = recommendationRepository;
        this.stressLevelRepository = stressLevelRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        List<Test> tests = testService.findAll();
        List<Student> students = studentRepository.findAll();

        model.addAttribute("user", user);
        model.addAttribute("tests", tests);
        model.addAttribute("questionCounts", buildQuestionCounts(tests));
        model.addAttribute("studentsCount", students.size());
        return "psychologist/dashboard";
    }

    @GetMapping("/tests")
    public String tests(@RequestParam(required = false) String q,
                        Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        String query = q == null ? "" : q.trim().toLowerCase();
        List<Test> tests = testService.findAll().stream()
                .filter(t -> query.isEmpty() || (t.getName() != null && t.getName().toLowerCase().contains(query)))
                .toList();
        model.addAttribute("user", user);
        model.addAttribute("tests", tests);
        model.addAttribute("questionCounts", buildQuestionCounts(tests));
        model.addAttribute("q", q);
        return "psychologist/tests";
    }

    private java.util.Map<String, Integer> buildQuestionCounts(List<Test> tests) {
        java.util.Map<String, Integer> map = new java.util.HashMap<>();
        for (Test t : tests) {
            map.put(t.getTestCode(), testService.getQuestionsByTestId(t.getTestCode()).size());
        }
        return map;
    }

    @GetMapping("/test/create")
    public String createTestForm(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("test", null);
        return "psychologist/test-form";
    }

    @GetMapping("/test/{code}/questions")
    public String testQuestions(@PathVariable("code") String testCode,
                                Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Test test = testService.findById(testCode).orElse(null);
        if (test == null) return "redirect:/psychologist/tests";

        List<Question> questions = testService.getQuestionsByTestId(testCode);
        java.util.Map<Long, List<Answer>> answersMap = new java.util.HashMap<>();
        for (Question q : questions) {
            answersMap.put(q.getQuestionNumber(),
                    testService.getAnswersByQuestionId(q.getQuestionNumber()));
        }
        model.addAttribute("user", user);
        model.addAttribute("test", test);
        model.addAttribute("questions", questions);
        model.addAttribute("answersMap", answersMap);
        return "psychologist/test-questions";
    }

    @PostMapping("/test/{code}/question/add")
    public String addQuestion(@PathVariable("code") String testCode,
                              @RequestParam String questionText) {
        List<Question> existing = testService.getQuestionsByTestId(testCode);
        short order = (short) (existing.size() + 1);
        testService.addQuestion(testCode, questionText, order);
        return "redirect:/psychologist/test/" + testCode + "/questions";
    }

    @PostMapping("/question/{qid}/answer/add")
    public String addAnswer(@PathVariable Long qid,
                            @RequestParam String testCode,
                            @RequestParam String answerText,
                            @RequestParam Short score) {
        List<Answer> existing = testService.getAnswersByQuestionId(qid);
        short order = (short) (existing.size() + 1);
        testService.addAnswer(qid, answerText, score, order);
        return "redirect:/psychologist/test/" + testCode + "/questions";
    }

    @PostMapping("/question/{qid}/delete")
    public String deleteQuestion(@PathVariable Long qid, @RequestParam String testCode) {
        testService.deleteQuestion(qid);
        return "redirect:/psychologist/test/" + testCode + "/questions";
    }

    @PostMapping("/answer/{aid}/delete")
    public String deleteAnswer(@PathVariable Long aid, @RequestParam String testCode) {
        testService.deleteAnswer(aid);
        return "redirect:/psychologist/test/" + testCode + "/questions";
    }

    @GetMapping("/student/{rbn}/dynamics")
    public String studentDynamics(@PathVariable("rbn") Long recordBookNumber,
                                  Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Student student = studentRepository.findById(recordBookNumber).orElse(null);
        if (student == null) return "redirect:/psychologist/students";

        List<TestResult> results = testService.getResultsByStudentId(recordBookNumber).stream()
                .sorted((a, b) -> a.getTakenAt().compareTo(b.getTakenAt()))
                .toList();
        double avg = results.stream().mapToInt(r -> r.getTotalScore() == null ? 0 : r.getTotalScore()).average().orElse(0.0);
        int min = results.stream().mapToInt(r -> r.getTotalScore() == null ? 0 : r.getTotalScore()).min().orElse(0);
        int max = results.stream().mapToInt(r -> r.getTotalScore() == null ? 0 : r.getTotalScore()).max().orElse(0);

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("results", results);
        model.addAttribute("avgScore", Math.round(avg * 10) / 10.0);
        model.addAttribute("minScore", min);
        model.addAttribute("maxScore", max);
        return "psychologist/student-dynamics";
    }

    @PostMapping("/test/create")
    public String createTest(@RequestParam String testCode,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String instructions) {
        testService.createTest(testCode, name, description, instructions);
        return "redirect:/psychologist/tests";
    }

    @GetMapping("/test/{code}/edit")
    public String editTestForm(@PathVariable("code") String testCode,
                               Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Test test = testService.findById(testCode).orElse(null);
        if (test == null) return "redirect:/psychologist/tests";

        model.addAttribute("user", user);
        model.addAttribute("test", test);
        return "psychologist/test-form";
    }

    @PostMapping("/test/{code}/update")
    public String updateTest(@PathVariable("code") String testCode,
                             @RequestParam String name,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String instructions) {
        testService.updateTest(testCode, name, description, instructions);
        return "redirect:/psychologist/tests";
    }

    @GetMapping("/students")
    public String students(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String group,
                           Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        String query = q == null ? "" : q.trim().toLowerCase();
        String groupFilter = group == null ? "" : group.trim().toLowerCase();
        List<Student> all = studentRepository.findAll();
        List<Student> filtered = all.stream()
                .filter(s -> query.isEmpty()
                        || (s.getFullName() != null && s.getFullName().toLowerCase().contains(query))
                        || (s.getUsername() != null && s.getUsername().toLowerCase().contains(query)))
                .filter(s -> groupFilter.isEmpty()
                        || (s.getGroupName() != null && s.getGroupName().toLowerCase().contains(groupFilter)))
                .toList();

        java.util.List<String> groups = all.stream()
                .map(Student::getGroupName)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("students", filtered);
        model.addAttribute("groups", groups);
        model.addAttribute("q", q);
        model.addAttribute("group", group);
        return "psychologist/students";
    }

    @GetMapping("/student/{rbn}/results")
    public String studentResults(@PathVariable("rbn") Long recordBookNumber,
                                 Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Student student = studentRepository.findById(recordBookNumber).orElse(null);
        if (student == null) return "redirect:/psychologist/students";

        List<TestResult> results = testService.getResultsByStudentId(recordBookNumber);

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("results", results);
        return "psychologist/student-results";
    }

    @GetMapping("/statistics")
    public String statistics(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        List<Test> tests = testService.findAll();
        List<TestResult> allResults = new java.util.ArrayList<>();
        for (Test t : tests) {
            allResults.addAll(testService.getResultsByTestId(t.getTestCode()));
        }

        java.util.Map<String, Long> levelCounts = new java.util.LinkedHashMap<>();
        for (StressLevel sl : stressLevelRepository.findAll()) {
            levelCounts.put(sl.getLevelName(), 0L);
        }
        for (TestResult r : allResults) {
            if (r.getStressLevel() != null) {
                levelCounts.merge(r.getStressLevel().getLevelName(), 1L, Long::sum);
            }
        }

        java.util.Map<String, Double> avgByTest = new java.util.HashMap<>();
        for (Test t : tests) {
            List<TestResult> rs = testService.getResultsByTestId(t.getTestCode());
            double avg = rs.stream()
                    .mapToInt(r -> r.getTotalScore() == null ? 0 : r.getTotalScore())
                    .average().orElse(0.0);
            avgByTest.put(t.getTestCode(), Math.round(avg * 10) / 10.0);
        }

        model.addAttribute("user", user);
        model.addAttribute("tests", tests);
        model.addAttribute("questionCounts", buildQuestionCounts(tests));
        model.addAttribute("totalResults", allResults.size());
        model.addAttribute("levelCounts", levelCounts);
        model.addAttribute("avgByTest", avgByTest);
        return "psychologist/statistics";
    }

    @GetMapping("/recommendation/create")
    public String createRecommendationForm(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        model.addAttribute("user", user);
        model.addAttribute("levels", stressLevelRepository.findAll());
        return "psychologist/recommendation-form";
    }

    @PostMapping("/recommendation/create")
    public String createRecommendation(@RequestParam String levelName,
                                       @RequestParam String recommendationText,
                                       Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        StressLevel stressLevel = stressLevelRepository.findById(levelName)
                .orElseThrow(() -> new IllegalArgumentException("Уровень стресса не найден"));

        Short orderNumber = (short) (recommendationRepository.count() + 1);
        testService.createRecommendation(stressLevel, recommendationText, orderNumber);

        return "redirect:/psychologist/statistics";
    }
}
