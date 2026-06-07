package ru.rrtu.mental_health_system.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rrtu.mental_health_system.model.*;
import ru.rrtu.mental_health_system.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * CRUD для всех таблиц БД, доступных администратору (модель данных из ПЗ).
 * Естественные ключи: grades.grade_name, indicators.indicator_name и т. д.
 */
@Service
public class AdminService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PsychologistRepository psychologistRepository;
    private final GradeRepository gradeRepository;
    private final TestRepository testRepository;
    private final IndicatorRepository indicatorRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TestResultRepository testResultRepository;
    private final RecommendationRepository recommendationRepository;
    private final TestNoteRepository testNoteRepository;
    private final ConsultationRepository consultationRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminService(RoleRepository roleRepository,
                        UserRepository userRepository,
                        StudentRepository studentRepository,
                        PsychologistRepository psychologistRepository,
                        GradeRepository gradeRepository,
                        TestRepository testRepository,
                        IndicatorRepository indicatorRepository,
                        QuestionRepository questionRepository,
                        AnswerRepository answerRepository,
                        TestResultRepository testResultRepository,
                        RecommendationRepository recommendationRepository,
                        TestNoteRepository testNoteRepository,
                        ConsultationRepository consultationRepository,
                        AuditLogRepository auditLogRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.psychologistRepository = psychologistRepository;
        this.gradeRepository = gradeRepository;
        this.testRepository = testRepository;
        this.indicatorRepository = indicatorRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.testResultRepository = testResultRepository;
        this.recommendationRepository = recommendationRepository;
        this.testNoteRepository = testNoteRepository;
        this.consultationRepository = consultationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    // ============================== Roles ==============================
    @Transactional(readOnly = true)
    public List<Role> findAllRoles() { return roleRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Role> findRoleById(String roleName) { return roleRepository.findById(roleName); }

    @Transactional
    public Role createRole(String name) { return createRole(name, null); }

    @Transactional
    public Role createRole(String name, String description) {
        return roleRepository.save(new Role(name, description));
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

    // ============================= Grades ==============================
    @Transactional(readOnly = true)
    public List<Grade> findAllGrades() { return gradeRepository.findAll(); }

    @Transactional
    public Grade createGrade(String name, Short minPercent, Short maxPercent) {
        return gradeRepository.save(new Grade(name, minPercent, maxPercent));
    }

    @Transactional
    public Grade updateGrade(String gradeName, String newName, Short minPercent, Short maxPercent) {
        Grade g = gradeRepository.findById(gradeName)
                .orElseThrow(() -> new IllegalArgumentException("Градация не найдена"));
        g.setGradeName(newName);
        g.setMinPercent(minPercent);
        g.setMaxPercent(maxPercent);
        return gradeRepository.save(g);
    }

    @Transactional
    public void deleteGrade(String gradeName) { gradeRepository.deleteById(gradeName); }

    // ============================ Indicators ===========================
    @Transactional(readOnly = true)
    public List<Indicator> findAllIndicators() { return indicatorRepository.findAll(); }

    @Transactional
    public Indicator createIndicator(String name, String description) {
        return indicatorRepository.save(new Indicator(name, description));
    }

    @Transactional
    public Indicator updateIndicator(String indicatorName, String newName, String description) {
        Indicator i = indicatorRepository.findById(indicatorName)
                .orElseThrow(() -> new IllegalArgumentException("Показатель не найден"));
        i.setIndicatorName(newName);
        i.setDescription(description);
        return indicatorRepository.save(i);
    }

    @Transactional
    public void deleteIndicator(String name) { indicatorRepository.deleteById(name); }

    // ============================ Tests ============================
    @Transactional(readOnly = true)
    public List<Test> findAllTests() { return testRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Test> findTestById(String testCode) { return testRepository.findById(testCode); }

    @Transactional
    public Test createTest(String testCode, String name, String description,
                           String instruction, Boolean isActive) {
        Test t = new Test();
        t.setTestCode(testCode);
        t.setName(name);
        t.setDescription(description);
        t.setInstruction(instruction);
        t.setIsActive(isActive != null ? isActive : true);
        return testRepository.save(t);
    }

    @Transactional
    public Test updateTest(String testCode, String name, String description,
                           String instruction, Boolean isActive) {
        Test t = testRepository.findById(testCode)
                .orElseThrow(() -> new IllegalArgumentException("Тест не найден"));
        t.setName(name);
        t.setDescription(description);
        t.setInstruction(instruction);
        t.setIsActive(isActive);
        return testRepository.save(t);
    }

    @Transactional
    public void deleteTest(String testCode) { testRepository.deleteById(testCode); }

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
    public Recommendation createRecommendation(String gradeName, String recommendationText, Short orderNumber) {
        Grade grade = gradeRepository.findById(gradeName)
                .orElseThrow(() -> new IllegalArgumentException("Градация не найдена"));
        Recommendation r = new Recommendation();
        r.setGrade(grade);
        r.setRecommendationText(recommendationText);
        r.setOrderNumber(orderNumber);
        return recommendationRepository.save(r);
    }

    @Transactional
    public Recommendation updateRecommendation(Long recommendationCode, String gradeName,
                                               String recommendationText, Short orderNumber) {
        Recommendation r = recommendationRepository.findById(recommendationCode)
                .orElseThrow(() -> new IllegalArgumentException("Рекомендация не найдена"));
        if (gradeName != null) {
            Grade grade = gradeRepository.findById(gradeName)
                    .orElseThrow(() -> new IllegalArgumentException("Градация не найдена"));
            r.setGrade(grade);
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
        s.setRiskGroup("нет");
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
    public void deleteStudent(Long recordBookNumber) { studentRepository.deleteById(recordBookNumber); }

    // =========================== Psychologists ===========================
    @Transactional(readOnly = true)
    public List<Psychologist> findAllPsychologists() { return psychologistRepository.findAll(); }

    @Transactional
    public Psychologist createPsychologist(String login, Long personnelNumber, String lastName,
                                           String firstName, String middleName, String specialization,
                                           String email, String phone) {
        User user = userRepository.findById(login)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        Psychologist p = new Psychologist();
        p.setPersonnelNumber(personnelNumber);
        p.setUser(user);
        p.setLastName(lastName);
        p.setFirstName(firstName);
        p.setMiddleName(middleName);
        p.setSpecialization(specialization);
        p.setEmail(email);
        p.setPhone(phone);
        return psychologistRepository.save(p);
    }

    @Transactional
    public Psychologist updatePsychologist(Long personnelNumber, String lastName, String firstName,
                                           String middleName, String specialization,
                                           String email, String phone) {
        Psychologist p = psychologistRepository.findById(personnelNumber)
                .orElseThrow(() -> new IllegalArgumentException("Психолог не найден"));
        p.setLastName(lastName);
        p.setFirstName(firstName);
        p.setMiddleName(middleName);
        p.setSpecialization(specialization);
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
                                       LocalDate dateTaken, String gradeName) {
        TestResult r = testResultRepository.findById(protocolNumber)
                .orElseThrow(() -> new IllegalArgumentException("Протокол не найден"));
        if (gradeName != null) {
            Grade g = gradeRepository.findById(gradeName)
                    .orElseThrow(() -> new IllegalArgumentException("Градация не найдена"));
            r.setGrade(g);
        }
        r.setTotalScore(totalScore);
        if (dateTaken != null) r.setTakenAt(dateTaken.atStartOfDay());
        return testResultRepository.save(r);
    }

    @Transactional
    public void deleteTestResult(Long protocolNumber) { testResultRepository.deleteById(protocolNumber); }

    // ============================ Audit Log ============================
    @Transactional(readOnly = true)
    public List<AuditLog> findAllAuditLogs() { return auditLogRepository.findAll(); }

    // ===================== Notes / Consultations =======================
    @Transactional(readOnly = true)
    public List<TestNote> findAllNotes() { return testNoteRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<Consultation> findAllConsultations() { return consultationRepository.findAll(); }
}
