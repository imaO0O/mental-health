package ru.rrtu.mental_health_system.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rrtu.mental_health_system.model.*;
import ru.rrtu.mental_health_system.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CRUD для всех таблиц БД, доступных администратору.
 * Все идентификаторы — естественные ключи согласно ПЗ.
 */
@Service
public class AdminService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PsychologistRepository psychologistRepository;
    private final StressLevelRepository stressLevelRepository;
    private final TestRepository testRepository;
    private final TestCategoryRepository testCategoryRepository;
    private final ResultStatusRepository resultStatusRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TestResultRepository testResultRepository;
    private final RecommendationRepository recommendationRepository;
    private final PsychologistNoteRepository psychologistNoteRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminService(RoleRepository roleRepository,
                        UserRepository userRepository,
                        StudentRepository studentRepository,
                        PsychologistRepository psychologistRepository,
                        StressLevelRepository stressLevelRepository,
                        TestRepository testRepository,
                        TestCategoryRepository testCategoryRepository,
                        ResultStatusRepository resultStatusRepository,
                        QuestionRepository questionRepository,
                        AnswerRepository answerRepository,
                        TestResultRepository testResultRepository,
                        RecommendationRepository recommendationRepository,
                        PsychologistNoteRepository psychologistNoteRepository,
                        AuditLogRepository auditLogRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.psychologistRepository = psychologistRepository;
        this.stressLevelRepository = stressLevelRepository;
        this.testRepository = testRepository;
        this.testCategoryRepository = testCategoryRepository;
        this.resultStatusRepository = resultStatusRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.testResultRepository = testResultRepository;
        this.recommendationRepository = recommendationRepository;
        this.psychologistNoteRepository = psychologistNoteRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ============================== Roles ==============================
    @Transactional(readOnly = true)
    public List<Role> findAllRoles() { return roleRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Role> findRoleById(String roleName) { return roleRepository.findById(roleName); }

    @Transactional
    public Role createRole(String name) {
        return createRole(name, null);
    }

    @Transactional
    public Role createRole(String name, String description) {
        Role role = new Role(name, description);
        return roleRepository.save(role);
    }

    @Transactional
    public Role updateRole(String roleName, String newName) {
        Role role = roleRepository.findById(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Роль не найдена"));
        role.setRoleName(newName);
        return roleRepository.save(role);
    }

    @Transactional
    public void deleteRole(String roleName) { roleRepository.deleteById(roleName); }

    // =========================== Stress Levels ==========================
    @Transactional(readOnly = true)
    public List<StressLevel> findAllStressLevels() { return stressLevelRepository.findAll(); }

    @Transactional
    public StressLevel createStressLevel(String name, Short minPercent, Short maxPercent) {
        StressLevel sl = new StressLevel(name, minPercent, maxPercent);
        return stressLevelRepository.save(sl);
    }

    @Transactional
    public StressLevel updateStressLevel(String levelName, String newName, Short minPercent, Short maxPercent) {
        StressLevel sl = stressLevelRepository.findById(levelName)
                .orElseThrow(() -> new IllegalArgumentException("Уровень стресса не найден"));
        sl.setLevelName(newName);
        sl.setMinPercent(minPercent);
        sl.setMaxPercent(maxPercent);
        return stressLevelRepository.save(sl);
    }

    @Transactional
    public void deleteStressLevel(String levelName) { stressLevelRepository.deleteById(levelName); }

    // ============================ Tests ============================
    @Transactional(readOnly = true)
    public List<Test> findAllTests() { return testRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Test> findTestById(String testCode) { return testRepository.findById(testCode); }

    @Transactional
    public Test createTest(String testCode, String name, String description,
                            String instructions, Boolean isActive) {
        Test t = new Test();
        t.setTestCode(testCode);
        t.setName(name);
        t.setDescription(description);
        t.setInstructions(instructions);
        t.setIsActive(isActive != null ? isActive : true);
        return testRepository.save(t);
    }

    @Transactional
    public Test updateTest(String testCode, String name, String description,
                            String instructions, Boolean isActive) {
        Test t = testRepository.findById(testCode)
                .orElseThrow(() -> new IllegalArgumentException("Тест не найден"));
        t.setName(name);
        t.setDescription(description);
        t.setInstructions(instructions);
        t.setIsActive(isActive);
        return testRepository.save(t);
    }

    @Transactional
    public void deleteTest(String testCode) { testRepository.deleteById(testCode); }

    // ========================== Test categories ==========================
    @Transactional(readOnly = true)
    public List<TestCategory> findAllCategories() { return testCategoryRepository.findAll(); }

    @Transactional
    public TestCategory createCategory(String name, String description) {
        return testCategoryRepository.save(new TestCategory(name, description));
    }

    @Transactional
    public void deleteCategory(String name) { testCategoryRepository.deleteById(name); }

    // ========================== Result statuses ==========================
    @Transactional(readOnly = true)
    public List<ResultStatus> findAllStatuses() { return resultStatusRepository.findAll(); }

    @Transactional
    public ResultStatus createStatus(String name, String description) {
        return resultStatusRepository.save(new ResultStatus(name, description));
    }

    @Transactional
    public void deleteStatus(String name) { resultStatusRepository.deleteById(name); }

    // ============================ Questions ============================
    @Transactional(readOnly = true)
    public List<Question> findAllQuestions() { return questionRepository.findAll(); }

    @Transactional
    public Question createQuestion(String testCode, String questionText, Short orderNumber) {
        Test test = testRepository.findById(testCode)
                .orElseThrow(() -> new IllegalArgumentException("Тест не найден"));
        Question q = new Question();
        q.setTest(test);
        q.setQuestionText(questionText);
        q.setOrderNumber(orderNumber);
        return questionRepository.save(q);
    }

    @Transactional
    public Question updateQuestion(Long questionNumber, String testCode, String questionText, Short orderNumber) {
        Question q = questionRepository.findById(questionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Вопрос не найден"));
        if (testCode != null) {
            Test t = testRepository.findById(testCode)
                    .orElseThrow(() -> new IllegalArgumentException("Тест не найден"));
            q.setTest(t);
        }
        q.setQuestionText(questionText);
        q.setOrderNumber(orderNumber);
        return questionRepository.save(q);
    }

    @Transactional
    public void deleteQuestion(Long questionNumber) { questionRepository.deleteById(questionNumber); }

    // ============================= Answers =============================
    @Transactional(readOnly = true)
    public List<Answer> findAllAnswers() { return answerRepository.findAll(); }

    @Transactional
    public Answer createAnswer(Long questionNumber, String answerText, Short score, Short orderNumber) {
        Question q = questionRepository.findById(questionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Вопрос не найден"));
        Answer a = new Answer();
        a.setQuestion(q);
        a.setAnswerText(answerText);
        a.setScore(score);
        a.setOrderNumber(orderNumber);
        return answerRepository.save(a);
    }

    @Transactional
    public Answer updateAnswer(Long answerCode, Long questionNumber, String answerText, Short score, Short orderNumber) {
        Answer a = answerRepository.findById(answerCode)
                .orElseThrow(() -> new IllegalArgumentException("Ответ не найден"));
        if (questionNumber != null) {
            Question q = questionRepository.findById(questionNumber)
                    .orElseThrow(() -> new IllegalArgumentException("Вопрос не найден"));
            a.setQuestion(q);
        }
        a.setAnswerText(answerText);
        a.setScore(score);
        a.setOrderNumber(orderNumber);
        return answerRepository.save(a);
    }

    @Transactional
    public void deleteAnswer(Long answerCode) { answerRepository.deleteById(answerCode); }

    // ========================== Recommendations ==========================
    @Transactional(readOnly = true)
    public List<Recommendation> findAllRecommendations() { return recommendationRepository.findAll(); }

    @Transactional
    public Recommendation createRecommendation(String levelName, String recommendationText, Short orderNumber) {
        StressLevel level = stressLevelRepository.findById(levelName)
                .orElseThrow(() -> new IllegalArgumentException("Уровень стресса не найден"));
        Recommendation r = new Recommendation();
        r.setStressLevel(level);
        r.setRecommendationText(recommendationText);
        r.setOrderNumber(orderNumber);
        return recommendationRepository.save(r);
    }

    @Transactional
    public Recommendation updateRecommendation(Long recommendationCode, String levelName,
                                               String recommendationText, Short orderNumber) {
        Recommendation r = recommendationRepository.findById(recommendationCode)
                .orElseThrow(() -> new IllegalArgumentException("Рекомендация не найдена"));
        if (levelName != null) {
            StressLevel level = stressLevelRepository.findById(levelName)
                    .orElseThrow(() -> new IllegalArgumentException("Уровень стресса не найден"));
            r.setStressLevel(level);
        }
        r.setRecommendationText(recommendationText);
        r.setOrderNumber(orderNumber);
        return recommendationRepository.save(r);
    }

    @Transactional
    public void deleteRecommendation(Long recommendationCode) {
        recommendationRepository.deleteById(recommendationCode);
    }

    // ============================= Students =============================
    @Transactional(readOnly = true)
    public List<Student> findAllStudents() { return studentRepository.findAll(); }

    @Transactional
    public Student createStudent(String login, Long recordBookNumber, String lastName,
                                  String firstName, String middleName, String groupName,
                                  String email, String phone) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Student s = new Student();
        s.setRecordBookNumber(recordBookNumber);
        s.setUser(user);
        s.setLastName(lastName);
        s.setFirstName(firstName);
        s.setMiddleName(middleName);
        s.setGroupName(groupName);
        s.setEmail(email);
        s.setPhone(phone);
        return studentRepository.save(s);
    }

    @Transactional
    public Student updateStudent(Long recordBookNumber, String lastName, String firstName,
                                  String middleName, String groupName,
                                  String email, String phone) {
        Student s = studentRepository.findById(recordBookNumber)
                .orElseThrow(() -> new IllegalArgumentException("Студент не найден"));
        s.setLastName(lastName);
        s.setFirstName(firstName);
        s.setMiddleName(middleName);
        s.setGroupName(groupName);
        s.setEmail(email);
        s.setPhone(phone);
        return studentRepository.save(s);
    }

    @Transactional
    public void deleteStudent(Long recordBookNumber) {
        studentRepository.deleteById(recordBookNumber);
    }

    // =========================== Psychologists ===========================
    @Transactional(readOnly = true)
    public List<Psychologist> findAllPsychologists() { return psychologistRepository.findAll(); }

    @Transactional
    public Psychologist createPsychologist(String login, Long personnelNumber, String lastName,
                                            String firstName, String middleName, String position,
                                            String email, String phone) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Psychologist p = new Psychologist();
        p.setPersonnelNumber(personnelNumber);
        p.setUser(user);
        p.setLastName(lastName);
        p.setFirstName(firstName);
        p.setMiddleName(middleName);
        p.setPosition(position);
        p.setEmail(email);
        p.setPhone(phone);
        return psychologistRepository.save(p);
    }

    @Transactional
    public Psychologist updatePsychologist(Long personnelNumber, String lastName, String firstName,
                                            String middleName, String position,
                                            String email, String phone) {
        Psychologist p = psychologistRepository.findById(personnelNumber)
                .orElseThrow(() -> new IllegalArgumentException("Психолог не найден"));
        p.setLastName(lastName);
        p.setFirstName(firstName);
        p.setMiddleName(middleName);
        p.setPosition(position);
        p.setEmail(email);
        p.setPhone(phone);
        return psychologistRepository.save(p);
    }

    @Transactional
    public void deletePsychologist(Long personnelNumber) {
        psychologistRepository.deleteById(personnelNumber);
    }

    // =========================== Test Results ===========================
    @Transactional(readOnly = true)
    public List<TestResult> findAllTestResults() { return testResultRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<TestResult> findTestResultById(Long protocolNumber) {
        return testResultRepository.findById(protocolNumber);
    }

    @Transactional
    public TestResult updateTestResult(Long protocolNumber, Short totalScore,
                                        LocalDate dateTaken, String levelName) {
        TestResult r = testResultRepository.findById(protocolNumber)
                .orElseThrow(() -> new IllegalArgumentException("Протокол не найден"));
        if (levelName != null) {
            StressLevel sl = stressLevelRepository.findById(levelName)
                    .orElseThrow(() -> new IllegalArgumentException("Уровень стресса не найден"));
            r.setStressLevel(sl);
        }
        r.setTotalScore(totalScore);
        if (dateTaken != null) r.setTakenAt(dateTaken.atStartOfDay());
        return testResultRepository.save(r);
    }

    @Transactional
    public void deleteTestResult(Long protocolNumber) {
        testResultRepository.deleteById(protocolNumber);
    }

    // ============================ Audit Log ============================
    @Transactional(readOnly = true)
    public List<AuditLog> findAllAuditLogs() { return auditLogRepository.findAll(); }

    // ======================== Psychologist Notes ========================
    @Transactional(readOnly = true)
    public List<PsychologistNote> findAllNotes() { return psychologistNoteRepository.findAll(); }
}
