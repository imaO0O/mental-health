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

import java.time.LocalDate;
import java.util.List;

/**
 * Идемпотентный засев справочных и демо-данных для модели данных из ПЗ:
 * показатели (indicators), градации (grades), кураторство, консультации,
 * заметки по тестированию.
 */
@Configuration
@Order(10)
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PsychologistRepository psychologistRepository;
    private final GradeRepository gradeRepository;
    private final TestRepository testRepository;
    private final IndicatorRepository indicatorRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final RecommendationRepository recommendationRepository;
    private final PsychologistSpecializationRepository psychologistSpecializationRepository;
    private final TestResultRepository testResultRepository;
    private final ConsultationRepository consultationRepository;
    private final TestNoteRepository testNoteRepository;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           StudentRepository studentRepository,
                           PsychologistRepository psychologistRepository,
                           GradeRepository gradeRepository,
                           TestRepository testRepository,
                           IndicatorRepository indicatorRepository,
                           QuestionRepository questionRepository,
                           AnswerRepository answerRepository,
                           RecommendationRepository recommendationRepository,
                           PsychologistSpecializationRepository psychologistSpecializationRepository,
                           TestResultRepository testResultRepository,
                           ConsultationRepository consultationRepository,
                           TestNoteRepository testNoteRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.psychologistRepository = psychologistRepository;
        this.gradeRepository = gradeRepository;
        this.testRepository = testRepository;
        this.indicatorRepository = indicatorRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.recommendationRepository = recommendationRepository;
        this.psychologistSpecializationRepository = psychologistSpecializationRepository;
        this.testResultRepository = testResultRepository;
        this.consultationRepository = consultationRepository;
        this.testNoteRepository = testNoteRepository;
    }

    @PostConstruct
    public void init() {
        log.info("DataInitializer: засев справочников и демо-данных...");
        PasswordEncoder enc = new BCryptPasswordEncoder();

        // === Роли ===
        Role roleStudent = upsertRole("STUDENT", "Студент — проходит тесты, видит свои протоколы и рекомендации");
        Role rolePsy     = upsertRole("PSYCHOLOGIST", "Психолог — управляет тестами, рекомендациями, ведёт студентов");
        Role roleAdmin   = upsertRole("ADMIN", "Администратор — управляет учётными записями и таблицами БД");

        // === Градации (по доле баллов от максимума) ===
        Grade gLow  = upsertGrade("Низкий", (short) 0,  (short) 25);
        Grade gMid  = upsertGrade("Средний", (short) 26, (short) 50);
        Grade gHigh = upsertGrade("Высокий", (short) 51, (short) 75);
        Grade gCrit = upsertGrade("Критический", (short) 76, (short) 100);

        // === Показатели психодиагностики ===
        Indicator iStress  = upsertIndicator("Стресс", "Уровень воспринимаемого стресса (методики PSS-10 и пр.)");
        Indicator iAnxiety = upsertIndicator("Тревога", "Уровень тревожности (Спилбергера-Ханина, BAI)");
        Indicator iDepr    = upsertIndicator("Депрессия", "Депрессивные состояния (шкала Бека, BDI)");
        Indicator iBurn    = upsertIndicator("Эмоциональное выгорание", "Эмоциональное истощение и выгорание (MBI)");

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

        // Специализации психологов (психолог × показатель)
        upsertSpec(psy0, iStress);
        upsertSpec(psy0, iBurn);
        upsertSpec(psy1, iStress);
        upsertSpec(psy1, iAnxiety);
        upsertSpec(psy2, iDepr);
        upsertSpec(psy2, iBurn);

        // === Студенты (с куратором, психотипом и группой риска) ===
        Student stu0 = upsertStudent("student", "student123", roleStudent, 202400000001L,
                "Тестовый", "Студент", "Студентович", "ИВТ-40",
                "Тревожный", "высокая", psy0, "student@test.ru", "+79000000000", enc);
        Student stu1 = upsertStudent("student1", "student123", roleStudent, 202400000002L,
                "Иванов", "Иван", "Сергеевич", "ИВТ-41",
                "Уравновешенный", "умеренная", psy0, "ivanov@student.ru", "+79001112233", enc);
        Student stu2 = upsertStudent("student2", "student123", roleStudent, 202400000003L,
                "Петров", "Пётр", "Александрович", "ИВТ-41",
                "Импульсивный", "нет", psy0, "petrov@student.ru", "+79001112244", enc);
        Student stu3 = upsertStudent("student3", "student123", roleStudent, 202400000004L,
                "Сидоров", "Александр", "Иванович", "ИВТ-42",
                "Замкнутый", "умеренная", psy1, "sidorov@student.ru", "+79001112255", enc);
        Student stu4 = upsertStudent("student4", "student123", roleStudent, 202400000005L,
                "Кузнецов", "Дмитрий", "Петрович", "ИВТ-42",
                "Уравновешенный", "нет", psy2, "kuznetsov@student.ru", "+79001112266", enc);

        // === Тесты (методики) ===
        Test bdi = upsertTest("BDI", "Шкала депрессии Бека",
                "Опросник для оценки уровня депрессии.",
                "Выберите вариант ответа, который лучше всего описывает ваше состояние за последние 2 недели.",
                psy2, iDepr);
        Question q1 = upsertQuestion(bdi, (short) 1, "Как вы оцениваете своё настроение?");
        Question q2 = upsertQuestion(bdi, (short) 2, "Как вы видите своё будущее?");
        Question q3 = upsertQuestion(bdi, (short) 3, "Чувствуете ли вы неудачи в жизни?");
        Question q4 = upsertQuestion(bdi, (short) 4, "Получаете ли вы удовольствие от жизни?");
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

        Test pss = upsertTest("PSS-10", "Шкала воспринимаемого стресса (Cohen)",
                "Опросник для оценки уровня воспринимаемого стресса за последний месяц.",
                "Оцените, насколько часто за последний месяц вы испытывали указанные ощущения.",
                psy1, iStress);
        addFrequencyScale(upsertQuestion(pss, (short) 1, "Как часто вас расстраивали неожиданные события?"));
        addFrequencyScale(upsertQuestion(pss, (short) 2, "Как часто вы чувствовали, что не контролируете важные вещи?"));
        addFrequencyScale(upsertQuestion(pss, (short) 3, "Как часто вы чувствовали нервозность и стресс?"));
        addFrequencyScale(upsertQuestion(pss, (short) 4, "Как часто трудности накапливались быстрее, чем вы успевали их решать?"));

        Test stai = upsertTest("STAI", "Шкала ситуативной тревожности (Спилбергера-Ханина)",
                "Опросник для оценки уровня ситуативной тревожности.",
                "Отметьте, насколько каждое утверждение соответствует вашему состоянию в данный момент.",
                psy1, iAnxiety);
        addAgreementScale(upsertQuestion(stai, (short) 1, "Я чувствую внутреннее напряжение"));
        addAgreementScale(upsertQuestion(stai, (short) 2, "Я нервничаю"));
        addAgreementScale(upsertQuestion(stai, (short) 3, "Я чувствую беспокойство"));
        addAgreementScale(upsertQuestion(stai, (short) 4, "Меня одолевают тревожные мысли"));

        Test mbi = upsertTest("MBI", "Опросник профессионального выгорания (Maslach)",
                "Опросник для оценки эмоционального истощения.",
                "Оцените, насколько часто вы испытываете описанные состояния в учебном процессе.",
                psy2, iBurn);
        addFrequencyScale(upsertQuestion(mbi, (short) 1, "Я чувствую эмоциональное истощение к концу учебной недели"));
        addFrequencyScale(upsertQuestion(mbi, (short) 2, "Я ощущаю себя «выжатым» после занятий"));
        addFrequencyScale(upsertQuestion(mbi, (short) 3, "Учёба отнимает у меня слишком много сил"));
        addFrequencyScale(upsertQuestion(mbi, (short) 4, "Я уже не получаю удовлетворения от учёбы"));

        // === Рекомендации (привязаны к градации) ===
        upsertRecommendation(gLow,  "Ваш показатель в норме. Продолжайте вести здоровый образ жизни, занимайтесь спортом и соблюдайте режим сна.", (short) 1, psy1);
        upsertRecommendation(gMid,  "Умеренный уровень. Рекомендуется больше отдыхать, практиковать техники релаксации и медитации.", (short) 2, psy1);
        upsertRecommendation(gHigh, "Высокий уровень. Рекомендуется обратиться к психологу для консультации и освоения техник саморегуляции.", (short) 3, psy2);
        upsertRecommendation(gCrit, "Критический уровень. Настоятельно рекомендуется немедленная консультация специалиста-психотерапевта.", (short) 4, psy2);

        // === Демо-протоколы тестирования ===
        TestResult p_stu0_bdi  = upsertProtocol(stu0, bdi,  (short) 8,  gHigh);
        upsertProtocol(stu1, bdi,  (short) 11, gCrit);
        upsertProtocol(stu2, bdi,  (short) 6,  gMid);
        upsertProtocol(stu3, bdi,  (short) 12, gCrit);
        TestResult p_stu0_pss  = upsertProtocol(stu0, pss,  (short) 5,  gMid);
        upsertProtocol(stu1, pss,  (short) 9,  gHigh);
        upsertProtocol(stu2, pss,  (short) 7,  gHigh);
        upsertProtocol(stu3, pss,  (short) 11, gCrit);
        upsertProtocol(stu0, stai, (short) 3,  gLow);
        TestResult p_stu1_stai = upsertProtocol(stu1, stai, (short) 8,  gHigh);
        upsertProtocol(stu3, stai, (short) 10, gCrit);
        upsertProtocol(stu2, mbi,  (short) 8,  gHigh);
        upsertProtocol(stu3, mbi,  (short) 11, gCrit);
        upsertProtocol(stu0, mbi,  (short) 12, gCrit);

        // === Консультации (разная давность — для списка «давно не на приёме») ===
        seedConsultation(psy0, stu0, 5,  "Плановая консультация. Обсудили учебную нагрузку, рекомендованы техники релаксации.");
        seedConsultation(psy0, stu1, 75, "Беседа по результатам тестирования. Назначено повторное обследование.");
        // stu2 (Петров) — у psy0 консультаций не было: попадёт в список «требует приёма».
        seedConsultation(psy1, stu3, 20, "Первичная консультация. Студент жалуется на тревожность перед сессией.");
        // stu4 (Кузнецов) — консультаций не было.

        // === Заметки по тестированию (привязаны к протоколу) ===
        seedNote(psy0, p_stu0_bdi,  3, "Высокий балл по шкале депрессии — рекомендована очная консультация.");
        seedNote(psy0, p_stu0_pss,  3, "Умеренный стресс, динамика положительная по сравнению с прошлым месяцем.");
        seedNote(psy0, p_stu1_stai, 10, "Повышенная ситуативная тревожность, связана с предстоящей сессией.");

        log.info("DataInitializer: засев завершён.");
    }

    // ==================================================================
    // Шкалы ответов
    // ==================================================================
    private void addFrequencyScale(Question q) {
        upsertAnswer(q, (short) 1, "Никогда",      (short) 0);
        upsertAnswer(q, (short) 2, "Иногда",       (short) 1);
        upsertAnswer(q, (short) 3, "Часто",        (short) 2);
        upsertAnswer(q, (short) 4, "Очень часто",  (short) 3);
    }

    private void addAgreementScale(Question q) {
        upsertAnswer(q, (short) 1, "Совсем не согласен", (short) 0);
        upsertAnswer(q, (short) 2, "Скорее не согласен", (short) 1);
        upsertAnswer(q, (short) 3, "Скорее согласен",    (short) 2);
        upsertAnswer(q, (short) 4, "Полностью согласен", (short) 3);
    }

    // ==================================================================
    // Хелперы upsert (идемпотентные)
    // ==================================================================
    private Role upsertRole(String name, String description) {
        return roleRepository.findById(name).orElseGet(() -> roleRepository.save(new Role(name, description)));
    }

    private Grade upsertGrade(String name, Short min, Short max) {
        return gradeRepository.findById(name).orElseGet(() -> gradeRepository.save(new Grade(name, min, max)));
    }

    private Indicator upsertIndicator(String name, String description) {
        return indicatorRepository.findById(name).orElseGet(() -> indicatorRepository.save(new Indicator(name, description)));
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

    private Student upsertStudent(String login, String pwd, Role role, Long recordBookNumber,
                                  String last, String first, String middle, String group,
                                  String psychType, String riskGroup, Psychologist curator,
                                  String email, String phone, PasswordEncoder enc) {
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
        s.setPsychType(psychType);
        s.setRiskGroup(riskGroup);
        s.setCurator(curator);
        s.setEmail(email);
        s.setPhone(phone);
        return studentRepository.save(s);
    }

    private Psychologist upsertPsychologist(String login, String pwd, Role role, Long personnelNumber,
                                            String last, String first, String middle, String specialization,
                                            String email, String phone, PasswordEncoder enc) {
        Psychologist existing = psychologistRepository.findByLogin(login);
        if (existing != null) return existing;
        User u = upsertUser(login, pwd, role, enc);
        Psychologist p = new Psychologist();
        p.setPersonnelNumber(personnelNumber);
        p.setUser(u);
        p.setLastName(last);
        p.setFirstName(first);
        p.setMiddleName(middle);
        p.setSpecialization(specialization);
        p.setEmail(email);
        p.setPhone(phone);
        return psychologistRepository.save(p);
    }

    private void upsertSpec(Psychologist p, Indicator ind) {
        PsychologistSpecializationId id = new PsychologistSpecializationId(
                p.getPersonnelNumber(), ind.getIndicatorName());
        if (psychologistSpecializationRepository.existsById(id)) return;
        psychologistSpecializationRepository.save(new PsychologistSpecialization(p, ind));
    }

    private Test upsertTest(String code, String name, String description, String instruction,
                            Psychologist author, Indicator indicator) {
        return testRepository.findById(code).orElseGet(() -> {
            Test t = new Test();
            t.setTestCode(code);
            t.setName(name);
            t.setDescription(description);
            t.setInstruction(instruction);
            t.setIsActive(true);
            t.setAuthor(author);
            t.setIndicator(indicator);
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

    private Recommendation upsertRecommendation(Grade grade, String text, Short order, Psychologist author) {
        return recommendationRepository.findAll().stream()
                .filter(r -> r.getGrade() != null
                        && grade.getGradeName().equals(r.getGrade().getGradeName())
                        && text.equals(r.getRecommendationText()))
                .findFirst()
                .orElseGet(() -> {
                    Recommendation r = new Recommendation();
                    r.setGrade(grade);
                    r.setRecommendationText(text);
                    r.setOrderNumber(order);
                    r.setAuthor(author);
                    return recommendationRepository.save(r);
                });
    }

    private TestResult upsertProtocol(Student student, Test test, Short totalScore, Grade grade) {
        return testResultRepository.findByStudentId(student.getRecordBookNumber()).stream()
                .filter(r -> r.getTest() != null && test.getTestCode().equals(r.getTest().getTestCode()))
                .findFirst()
                .orElseGet(() -> {
                    TestResult r = new TestResult();
                    r.setStudent(student);
                    r.setTest(test);
                    r.setTotalScore(totalScore);
                    r.setGrade(grade);
                    return testResultRepository.save(r);
                });
    }

    private void seedConsultation(Psychologist psy, Student student, int daysAgo, String text) {
        List<Consultation> existing = consultationRepository.findByStudent(student.getRecordBookNumber());
        boolean has = existing.stream().anyMatch(c -> c.getPsychologist() != null
                && psy.getPersonnelNumber().equals(c.getPsychologist().getPersonnelNumber()));
        if (has) return;
        Consultation c = new Consultation();
        c.setPsychologist(psy);
        c.setStudent(student);
        c.setConsultationDate(LocalDate.now().minusDays(daysAgo));
        c.setConsultationText(text);
        consultationRepository.save(c);
    }

    private void seedNote(Psychologist psy, TestResult protocol, int daysAgo, String text) {
        if (protocol == null) return;
        if (!testNoteRepository.findByProtocol(protocol.getProtocolNumber()).isEmpty()) return;
        TestNote n = new TestNote();
        n.setPsychologist(psy);
        n.setProtocol(protocol);
        n.setNoteDate(LocalDate.now().minusDays(daysAgo));
        n.setNoteText(text);
        testNoteRepository.save(n);
    }
}
