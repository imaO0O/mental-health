package ru.rrtu.mental_health_system.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.rrtu.mental_health_system.model.*;
import ru.rrtu.mental_health_system.repository.*;

/**
 * Идемпотентный засев справочных и демо-данных, согласованный
 * с новой схемой БД (естественные первичные ключи).
 */
@Configuration
@Order(10)
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

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
    private final RecommendationRepository recommendationRepository;
    private final PsychologistSpecializationRepository psychologistSpecializationRepository;
    private final TestResultRepository testResultRepository;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           StudentRepository studentRepository,
                           PsychologistRepository psychologistRepository,
                           StressLevelRepository stressLevelRepository,
                           TestRepository testRepository,
                           TestCategoryRepository testCategoryRepository,
                           ResultStatusRepository resultStatusRepository,
                           QuestionRepository questionRepository,
                           AnswerRepository answerRepository,
                           RecommendationRepository recommendationRepository,
                           PsychologistSpecializationRepository psychologistSpecializationRepository,
                           TestResultRepository testResultRepository) {
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
        this.recommendationRepository = recommendationRepository;
        this.psychologistSpecializationRepository = psychologistSpecializationRepository;
        this.testResultRepository = testResultRepository;
    }

    @PostConstruct
    public void init() {
        log.info("DataInitializer: засев справочников и демо-данных...");
        PasswordEncoder enc = new BCryptPasswordEncoder();

        // === Роли ===
        Role roleStudent  = upsertRole("STUDENT", "Студент — проходит тесты, видит свои протоколы и рекомендации");
        Role rolePsy      = upsertRole("PSYCHOLOGIST", "Психолог — управляет тестами, рекомендациями, ведёт студентов");
        Role roleAdmin    = upsertRole("ADMIN", "Администратор — управляет учётными записями и таблицами БД");

        // === Уровни стресса ===
        StressLevel sLow  = upsertStressLevel("Низкий", (short) 0,  (short) 25);
        StressLevel sMid  = upsertStressLevel("Средний", (short) 26, (short) 50);
        StressLevel sHigh = upsertStressLevel("Высокий", (short) 51, (short) 75);
        StressLevel sCrit = upsertStressLevel("Критический", (short) 76, (short) 100);

        // === Категории тестов ===
        TestCategory cStress  = upsertCategory("Стресс", "Методики оценки уровня стресса (PSS-10 и пр.)");
        TestCategory cAnxiety = upsertCategory("Тревога", "Методики оценки тревожности (Спилбергера-Ханина, BAI)");
        TestCategory cDepr    = upsertCategory("Депрессия", "Методики оценки депрессивных состояний (BDI)");
        TestCategory cBurn    = upsertCategory("Эмоциональное выгорание", "Методики оценки выгорания (MBI и пр.)");

        // === Статусы протоколов ===
        upsertStatus("Завершён", "Протокол полностью завершён студентом");
        upsertStatus("Прерван", "Студент прервал прохождение, не дойдя до конца");
        upsertStatus("Аннулирован", "Психолог отменил протокол");
        upsertStatus("Требует консультации", "Высокий результат — рекомендуется очная консультация");

        // === Учётные записи ===
        upsertUser("admin", "admin123", roleAdmin, enc);

        // === Психологи ===
        Psychologist psy0 = upsertPsychologist("psychologist", "psychologist123", rolePsy,
                100000L, "Петрова", "Анна", "Сергеевна", "Ведущий психолог",
                "petrova@mentalhealth.ru", "+79002223300", enc);
        Psychologist psy1 = upsertPsychologist("psychologist1", "psychologist123", rolePsy,
                100001L, "Соколова", "Елена", "Викторовна", "Клинический психолог",
                "sokolova@mentalhealth.ru", "+79002223344", enc);
        Psychologist psy2 = upsertPsychologist("psychologist2", "psychologist123", rolePsy,
                100002L, "Лебедев", "Михаил", "Юрьевич", "Психотерапевт",
                "lebedev@mentalhealth.ru", "+79002223355", enc);

        // Специализации психологов
        upsertSpec(psy0, cStress);
        upsertSpec(psy0, cBurn);
        upsertSpec(psy1, cStress);
        upsertSpec(psy1, cAnxiety);
        upsertSpec(psy2, cDepr);
        upsertSpec(psy2, cBurn);

        // === Студенты ===
        Student stu0 = upsertStudent("student", "student123", roleStudent,
                202400000001L, "Тестовый", "Студент", "Студентович", "ИВТ-40",
                "student@test.ru", "+79000000000", enc);
        Student stu1 = upsertStudent("student1", "student123", roleStudent,
                202400000002L, "Иванов", "Иван", "Сергеевич", "ИВТ-41",
                "ivanov@student.ru", "+79001112233", enc);
        Student stu2 = upsertStudent("student2", "student123", roleStudent,
                202400000003L, "Петров", "Пётр", "Александрович", "ИВТ-41",
                "petrov@student.ru", "+79001112244", enc);
        Student stu3 = upsertStudent("student3", "student123", roleStudent,
                202400000004L, "Сидоров", "Александр", "Иванович", "ИВТ-42",
                "sidorov@student.ru", "+79001112255", enc);
        upsertStudent("student4", "student123", roleStudent,
                202400000005L, "Кузнецов", "Дмитрий", "Петрович", "ИВТ-42",
                "kuznetsov@student.ru", "+79001112266", enc);

        // === Демо-тест ===
        Test bdi = upsertTest("BDI", "Шкала депрессии Бека",
                "Опросник для оценки уровня депрессии",
                "Выберите вариант ответа, который лучше всего описывает ваше состояние за последние 2 недели.",
                psy2, cDepr);

        // Вопросы
        Question q1 = upsertQuestion(bdi, (short) 1, "Как вы оцениваете своё настроение?");
        Question q2 = upsertQuestion(bdi, (short) 2, "Как вы видите своё будущее?");
        Question q3 = upsertQuestion(bdi, (short) 3, "Чувствуете ли вы неудачи в жизни?");
        Question q4 = upsertQuestion(bdi, (short) 4, "Получаете ли вы удовольствие от жизни?");

        // Ответы
        upsertAnswer(q1, (short) 1, "Я не чувствую грусти", (short) 0);
        upsertAnswer(q1, (short) 2, "Я чувствую грусть время от времени", (short) 1);
        upsertAnswer(q1, (short) 3, "Я чувствую грусть постоянно", (short) 2);
        upsertAnswer(q1, (short) 4, "Я очень подавлен и несчастен", (short) 3);

        upsertAnswer(q2, (short) 1, "Я не разочарован в будущем", (short) 0);
        upsertAnswer(q2, (short) 2, "Я чувствую разочарование в будущем", (short) 1);
        upsertAnswer(q2, (short) 3, "Я не ожидаю ничего хорошего", (short) 2);
        upsertAnswer(q2, (short) 4, "Будущее кажется безнадёжным", (short) 3);

        upsertAnswer(q3, (short) 1, "Я не чувствую себя неудачником", (short) 0);
        upsertAnswer(q3, (short) 2, "Я чувствую неудачи чаще, чем другие", (short) 1);
        upsertAnswer(q3, (short) 3, "Я чувствую много неудач", (short) 2);
        upsertAnswer(q3, (short) 4, "Я полный неудачник", (short) 3);

        upsertAnswer(q4, (short) 1, "Я получаю удовольствие от жизни", (short) 0);
        upsertAnswer(q4, (short) 2, "Я получаю меньше удовольствия", (short) 1);
        upsertAnswer(q4, (short) 3, "Я получаю очень мало удовольствия", (short) 2);
        upsertAnswer(q4, (short) 4, "Я не получаю никакого удовольствия", (short) 3);

        // === Рекомендации ===
        upsertRecommendation(sLow,  "Ваш уровень стресса в норме. Продолжайте вести здоровый образ жизни, занимайтесь спортом и соблюдайте режим сна.", (short) 1, psy1);
        upsertRecommendation(sMid,  "У вас умеренный уровень стресса. Рекомендуется больше отдыхать, практиковать техники релаксации и медитации.", (short) 2, psy1);
        upsertRecommendation(sHigh, "У вас высокий уровень стресса. Рекомендуется обратиться к психологу для консультации и освоения техник управления стрессом.", (short) 3, psy2);
        upsertRecommendation(sCrit, "У вас критический уровень стресса. Настоятельно рекомендуется немедленная консультация специалиста-психотерапевта.", (short) 4, psy2);

        // === Демо-протоколы тестирования (для лабораторной №3) ===
        // Чтобы вкладки 1, 3, 5 имели данные для демонстрации эффекта процедур.
        upsertProtocol(stu0, bdi, (short) 8,  sMid);
        upsertProtocol(stu0, bdi, (short) 4,  sLow);
        upsertProtocol(stu1, bdi, (short) 11, sHigh);
        upsertProtocol(stu2, bdi, (short) 6,  sMid);
        upsertProtocol(stu3, bdi, (short) 12, sCrit);

        log.info("DataInitializer: засев завершён.");
    }

    private TestResult upsertProtocol(Student student, Test test, Short totalScore, StressLevel level) {
        // Идемпотентно: если у студента уже есть протокол по этому тесту — не дублируем.
        return testResultRepository.findByStudentId(student.getRecordBookNumber()).stream()
                .filter(r -> r.getTest() != null
                        && test.getTestCode().equals(r.getTest().getTestCode()))
                .findFirst()
                .orElseGet(() -> {
                    TestResult r = new TestResult();
                    r.setStudent(student);
                    r.setTest(test);
                    r.setTotalScore(totalScore);
                    r.setStressLevel(level);
                    return testResultRepository.save(r);
                });
    }

    // ==================================================================
    // Хелперы upsert (идемпотентные)
    // ==================================================================

    private Role upsertRole(String name, String description) {
        return roleRepository.findById(name).orElseGet(() -> {
            Role r = new Role(name, description);
            return roleRepository.save(r);
        });
    }

    private StressLevel upsertStressLevel(String name, Short min, Short max) {
        return stressLevelRepository.findById(name).orElseGet(() -> {
            StressLevel sl = new StressLevel(name, min, max);
            return stressLevelRepository.save(sl);
        });
    }

    private TestCategory upsertCategory(String name, String description) {
        return testCategoryRepository.findById(name).orElseGet(() -> {
            TestCategory tc = new TestCategory(name, description);
            return testCategoryRepository.save(tc);
        });
    }

    private ResultStatus upsertStatus(String name, String description) {
        return resultStatusRepository.findById(name).orElseGet(() -> {
            ResultStatus rs = new ResultStatus(name, description);
            return resultStatusRepository.save(rs);
        });
    }

    private User upsertUser(String login, String pwd, Role role, PasswordEncoder enc) {
        return userRepository.findById(login).orElseGet(() -> {
            User u = new User();
            u.setLogin(login);
            u.setPasswordHash(enc.encode(pwd));
            u.setRole(role);
            return userRepository.save(u);
        });
    }

    private Student upsertStudent(String login, String pwd, Role role,
                                  Long recordBookNumber,
                                  String last, String first, String middle,
                                  String group, String email, String phone,
                                  PasswordEncoder enc) {
        Student existing = studentRepository.findByLogin(login);
        if (existing != null) return existing;
        User u = upsertUser(login, pwd, role, enc);
        Student s = new Student();
        s.setRecordBookNumber(recordBookNumber);
        s.setUser(u);
        s.setLastName(last);
        s.setFirstName(first);
        s.setMiddleName(middle);
        s.setGroupName(group);
        s.setEmail(email);
        s.setPhone(phone);
        return studentRepository.save(s);
    }

    private Psychologist upsertPsychologist(String login, String pwd, Role role,
                                            Long personnelNumber,
                                            String last, String first, String middle,
                                            String position, String email, String phone,
                                            PasswordEncoder enc) {
        Psychologist existing = psychologistRepository.findByLogin(login);
        if (existing != null) return existing;
        User u = upsertUser(login, pwd, role, enc);
        Psychologist p = new Psychologist();
        p.setPersonnelNumber(personnelNumber);
        p.setUser(u);
        p.setLastName(last);
        p.setFirstName(first);
        p.setMiddleName(middle);
        p.setPosition(position);
        p.setEmail(email);
        p.setPhone(phone);
        return psychologistRepository.save(p);
    }

    private void upsertSpec(Psychologist p, TestCategory c) {
        PsychologistSpecializationId id = new PsychologistSpecializationId(
                p.getPersonnelNumber(), c.getCategoryName());
        if (psychologistSpecializationRepository.existsById(id)) return;
        psychologistSpecializationRepository.save(new PsychologistSpecialization(p, c));
    }

    private Test upsertTest(String code, String name, String description, String instructions,
                            Psychologist author, TestCategory category) {
        return testRepository.findById(code).orElseGet(() -> {
            Test t = new Test();
            t.setTestCode(code);
            t.setName(name);
            t.setDescription(description);
            t.setInstructions(instructions);
            t.setIsActive(true);
            t.setAuthor(author);
            t.setCategory(category);
            return testRepository.save(t);
        });
    }

    private Question upsertQuestion(Test t, Short order, String text) {
        return questionRepository.findByTestCode(t.getTestCode()).stream()
                .filter(q -> order.equals(q.getOrderNumber()))
                .findFirst()
                .orElseGet(() -> {
                    Question q = new Question();
                    q.setTest(t);
                    q.setOrderNumber(order);
                    q.setQuestionText(text);
                    return questionRepository.save(q);
                });
    }

    private Answer upsertAnswer(Question q, Short order, String text, Short score) {
        return answerRepository.findByQuestionNumber(q.getQuestionNumber()).stream()
                .filter(a -> order.equals(a.getOrderNumber()))
                .findFirst()
                .orElseGet(() -> {
                    Answer a = new Answer();
                    a.setQuestion(q);
                    a.setOrderNumber(order);
                    a.setAnswerText(text);
                    a.setScore(score);
                    return answerRepository.save(a);
                });
    }

    private Recommendation upsertRecommendation(StressLevel level, String text, Short order, Psychologist author) {
        return recommendationRepository.findAll().stream()
                .filter(r -> r.getStressLevel() != null
                        && level.getLevelName().equals(r.getStressLevel().getLevelName())
                        && text.equals(r.getRecommendationText()))
                .findFirst()
                .orElseGet(() -> {
                    Recommendation r = new Recommendation();
                    r.setStressLevel(level);
                    r.setRecommendationText(text);
                    r.setOrderNumber(order);
                    r.setAuthor(author);
                    return recommendationRepository.save(r);
                });
    }
}
