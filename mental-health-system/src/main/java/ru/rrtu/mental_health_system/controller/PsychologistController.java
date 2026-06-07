package ru.rrtu.mental_health_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.rrtu.mental_health_system.model.*;
import ru.rrtu.mental_health_system.repository.ConsultationRepository;
import ru.rrtu.mental_health_system.repository.GradeRepository;
import ru.rrtu.mental_health_system.repository.PsychologistRepository;
import ru.rrtu.mental_health_system.repository.RecommendationRepository;
import ru.rrtu.mental_health_system.repository.StudentRepository;
import ru.rrtu.mental_health_system.repository.TestNoteRepository;
import ru.rrtu.mental_health_system.service.TestService;
import ru.rrtu.mental_health_system.service.UserService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/psychologist")
public class PsychologistController {

    /** Порог давности консультации (дней) для попадания в список «требует приёма». */
    private static final long OVERDUE_DAYS = 30;

    private final UserService userService;
    private final TestService testService;
    private final StudentRepository studentRepository;
    private final RecommendationRepository recommendationRepository;
    private final GradeRepository gradeRepository;
    private final ConsultationRepository consultationRepository;
    private final TestNoteRepository testNoteRepository;
    private final PsychologistRepository psychologistRepository;

    public PsychologistController(UserService userService,
                                  TestService testService,
                                  StudentRepository studentRepository,
                                  RecommendationRepository recommendationRepository,
                                  GradeRepository gradeRepository,
                                  ConsultationRepository consultationRepository,
                                  TestNoteRepository testNoteRepository,
                                  PsychologistRepository psychologistRepository) {
        this.userService = userService;
        this.testService = testService;
        this.studentRepository = studentRepository;
        this.recommendationRepository = recommendationRepository;
        this.gradeRepository = gradeRepository;
        this.consultationRepository = consultationRepository;
        this.testNoteRepository = testNoteRepository;
        this.psychologistRepository = psychologistRepository;
    }

    private Psychologist currentPsychologist(User user) {
        if (user == null) return null;
        return psychologistRepository.findByLogin(user.getLogin());
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        Psychologist psy = currentPsychologist(user);

        List<Test> tests = testService.findAll();
        List<Student> curated = curatedStudents(psy);

        model.addAttribute("user", user);
        model.addAttribute("psychologist", psy);
        model.addAttribute("tests", tests);
        model.addAttribute("questionCounts", buildQuestionCounts(tests));
        model.addAttribute("studentsCount", curated.size());
        model.addAttribute("curated", curated);
        return "psychologist/dashboard";
    }

    private List<Student> curatedStudents(Psychologist psy) {
        if (psy == null) return studentRepository.findAll();
        return studentRepository.findAll().stream()
                .filter(s -> s.getCurator() != null
                        && psy.getPersonnelNumber().equals(s.getCurator().getPersonnelNumber()))
                .toList();
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

        java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");
        java.util.List<String> chartDates  = new java.util.ArrayList<>();
        java.util.List<Integer> chartScores = new java.util.ArrayList<>();
        java.util.List<String> chartTests  = new java.util.ArrayList<>();
        for (TestResult r : results) {
            chartDates.add(r.getTakenAt() != null ? r.getTakenAt().format(df) : "—");
            chartScores.add(r.getTotalScore() == null ? 0 : r.getTotalScore().intValue());
            chartTests.add(r.getTest() != null ? r.getTest().getName() : "—");
        }

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("results", results);
        model.addAttribute("avgScore", Math.round(avg * 10) / 10.0);
        model.addAttribute("minScore", min);
        model.addAttribute("maxScore", max);
        model.addAttribute("chartDates", chartDates);
        model.addAttribute("chartScores", chartScores);
        model.addAttribute("chartTests", chartTests);
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

    /**
     * Список студентов. По умолчанию — закреплённые за психологом обучающиеся,
     * отсортированные по давности последней консультации (сначала те, кого давно
     * не было на приёме). При заданном поиске q/group — адресный поиск по всем студентам.
     */
    @GetMapping("/students")
    public String students(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String group,
                           Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        Psychologist psy = currentPsychologist(user);

        String query = q == null ? "" : q.trim().toLowerCase();
        String groupFilter = group == null ? "" : group.trim().toLowerCase();
        boolean searching = !query.isEmpty() || !groupFilter.isEmpty();

        List<Student> base = searching ? studentRepository.findAll() : curatedStudents(psy);
        List<Student> filtered = base.stream()
                .filter(s -> query.isEmpty()
                        || (s.getFullName() != null && s.getFullName().toLowerCase().contains(query))
                        || (s.getUsername() != null && s.getUsername().toLowerCase().contains(query)))
                .filter(s -> groupFilter.isEmpty()
                        || (s.getGroupName() != null && s.getGroupName().toLowerCase().contains(groupFilter)))
                .toList();

        LocalDate today = LocalDate.now();
        java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
        for (Student s : filtered) {
            LocalDate last = consultationRepository.findLastDate(s.getRecordBookNumber());
            Long daysSince = last == null ? null : ChronoUnit.DAYS.between(last, today);
            boolean overdue = last == null || daysSince > OVERDUE_DAYS;
            // текущая градация = градация последнего протокола
            TestResult lastProtocol = lastProtocol(s.getRecordBookNumber());
            String grade = (lastProtocol != null && lastProtocol.getGrade() != null)
                    ? lastProtocol.getGrade().getGradeName() : null;

            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("rbn", s.getRecordBookNumber());
            row.put("fio", s.getFullName());
            row.put("group", s.getGroupName());
            row.put("lastDate", last);
            row.put("daysSince", daysSince);
            row.put("overdue", overdue);
            row.put("grade", grade);
            row.put("riskGroup", s.getRiskGroup());
            rows.add(row);
        }
        // Сначала «требует приёма» (overdue), затем по возрастанию давности.
        rows.sort((a, b) -> {
            boolean oa = Boolean.TRUE.equals(a.get("overdue"));
            boolean ob = Boolean.TRUE.equals(b.get("overdue"));
            if (oa != ob) return oa ? -1 : 1;
            Long da = (Long) a.get("daysSince");
            Long db = (Long) b.get("daysSince");
            long va = da == null ? Long.MAX_VALUE : da;
            long vb = db == null ? Long.MAX_VALUE : db;
            return Long.compare(vb, va);
        });
        long overdueCount = rows.stream().filter(r -> Boolean.TRUE.equals(r.get("overdue"))).count();

        java.util.List<String> groups = studentRepository.findAll().stream()
                .map(Student::getGroupName).filter(java.util.Objects::nonNull).distinct().sorted().toList();

        model.addAttribute("user", user);
        model.addAttribute("rows", rows);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("overdueDays", OVERDUE_DAYS);
        model.addAttribute("searching", searching);
        model.addAttribute("groups", groups);
        model.addAttribute("q", q);
        model.addAttribute("group", group);
        return "psychologist/students";
    }

    private TestResult lastProtocol(Long rbn) {
        TestResult last = null;
        for (TestResult r : testService.getResultsByStudentId(rbn)) {
            if (last == null || (r.getTakenAt() != null && last.getTakenAt() != null
                    && r.getTakenAt().isAfter(last.getTakenAt()))) {
                last = r;
            }
        }
        return last;
    }

    @GetMapping("/student/{rbn}/results")
    public String studentResults(@PathVariable("rbn") Long recordBookNumber,
                                 Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Student student = studentRepository.findById(recordBookNumber).orElse(null);
        if (student == null) return "redirect:/psychologist/students";

        List<TestResult> results = testService.getResultsByStudentId(recordBookNumber);
        List<TestNote> notes = testNoteRepository.findByStudent(recordBookNumber);
        List<Consultation> consultations = consultationRepository.findByStudent(recordBookNumber);

        model.addAttribute("user", user);
        model.addAttribute("student", student);
        model.addAttribute("results", results);
        model.addAttribute("notes", notes);
        model.addAttribute("consultations", consultations);
        return "psychologist/student-results";
    }

    /** Добавление заметки по протоколу тестирования. */
    @PostMapping("/student/{rbn}/note")
    public String addNote(@PathVariable("rbn") Long recordBookNumber,
                          @RequestParam Long protocolNumber,
                          @RequestParam String noteText,
                          Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        Psychologist psy = currentPsychologist(user);
        TestResult protocol = testService.findResultById(protocolNumber).orElse(null);
        if (psy != null && protocol != null && noteText != null && !noteText.isBlank()) {
            TestNote n = new TestNote();
            n.setPsychologist(psy);
            n.setProtocol(protocol);
            n.setNoteText(noteText);
            testNoteRepository.save(n);
        }
        return "redirect:/psychologist/student/" + recordBookNumber + "/results";
    }

    /** Регистрация консультации со студентом. */
    @PostMapping("/student/{rbn}/consultation")
    public String addConsultation(@PathVariable("rbn") Long recordBookNumber,
                                  @RequestParam String consultationText,
                                  Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        Psychologist psy = currentPsychologist(user);
        Student student = studentRepository.findById(recordBookNumber).orElse(null);
        if (psy != null && student != null && consultationText != null && !consultationText.isBlank()) {
            Consultation c = new Consultation();
            c.setPsychologist(psy);
            c.setStudent(student);
            c.setConsultationText(consultationText);
            consultationRepository.save(c);
        }
        return "redirect:/psychologist/student/" + recordBookNumber + "/results";
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
        for (Grade g : gradeRepository.findAll()) {
            levelCounts.put(g.getGradeName(), 0L);
        }
        for (TestResult r : allResults) {
            if (r.getGrade() != null) {
                levelCounts.merge(r.getGrade().getGradeName(), 1L, Long::sum);
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

        java.util.List<String> testNamesList = new java.util.ArrayList<>();
        java.util.List<Double> testAvgsList = new java.util.ArrayList<>();
        for (Test t : tests) {
            testNamesList.add(t.getName());
            testAvgsList.add(avgByTest.getOrDefault(t.getTestCode(), 0.0));
        }

        java.util.Set<String> alarmLevels =
                new java.util.HashSet<>(java.util.Arrays.asList("Высокий", "Критический"));
        java.util.List<java.util.Map<String, Object>> studentRows = new java.util.ArrayList<>();
        for (Student s : studentRepository.findAll()) {
            TestResult last = lastProtocol(s.getRecordBookNumber());
            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("fio", s.getFullName());
            row.put("group", s.getGroupName());
            if (last != null) {
                row.put("hasResult", true);
                row.put("date", last.getTakenAt());
                row.put("testName", last.getTest() != null ? last.getTest().getName() : "—");
                row.put("score", last.getTotalScore());
                String lvl = last.getGrade() != null ? last.getGrade().getGradeName() : null;
                row.put("level", lvl);
                row.put("alarm", lvl != null && alarmLevels.contains(lvl));
            } else {
                row.put("hasResult", false);
                row.put("date", null);
                row.put("testName", "—");
                row.put("score", null);
                row.put("level", null);
                row.put("alarm", false);
            }
            studentRows.add(row);
        }
        long alarmCount = studentRows.stream()
                .filter(r -> Boolean.TRUE.equals(r.get("alarm"))).count();

        model.addAttribute("user", user);
        model.addAttribute("tests", tests);
        model.addAttribute("questionCounts", buildQuestionCounts(tests));
        model.addAttribute("totalResults", allResults.size());
        model.addAttribute("levelCounts", levelCounts);
        model.addAttribute("avgByTest", avgByTest);
        model.addAttribute("testNamesList", testNamesList);
        model.addAttribute("testAvgsList", testAvgsList);
        model.addAttribute("studentRows", studentRows);
        model.addAttribute("alarmCount", alarmCount);
        return "psychologist/statistics";
    }

    @GetMapping("/recommendation/create")
    public String createRecommendationForm(Model model, Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";
        model.addAttribute("user", user);
        model.addAttribute("levels", gradeRepository.findAll());
        model.addAttribute("recommendations", testService.getAllRecommendations());
        return "psychologist/recommendation-form";
    }

    @PostMapping("/recommendation/create")
    public String createRecommendation(@RequestParam String levelName,
                                       @RequestParam String recommendationText,
                                       Authentication authentication) {
        User user = userService.findByLogin(authentication.getName()).orElse(null);
        if (user == null) return "redirect:/login";

        Grade grade = gradeRepository.findById(levelName)
                .orElseThrow(() -> new IllegalArgumentException("Градация не найдена"));
        Short orderNumber = (short) (recommendationRepository.count() + 1);
        testService.createRecommendation(grade, recommendationText, orderNumber);
        return "redirect:/psychologist/recommendation/create";
    }
}
