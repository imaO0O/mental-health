package ru.rrtu.mental_health_system.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rrtu.mental_health_system.model.*;
import ru.rrtu.mental_health_system.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Сервис тестов и протоколов тестирования.
 * Работает на новой схеме с естественными ключами:
 *   tests.test_code (String), questions.question_number (Long),
 *   answers.answer_code (Long), test_protocols.protocol_number (Long),
 *   stress_levels.level_name (String).
 */
@Service
public class TestService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TestResultRepository testResultRepository;
    private final ResultAnswerRepository resultAnswerRepository;
    private final RecommendationRepository recommendationRepository;
    private final StressLevelRepository stressLevelRepository;
    private final ResultStatusRepository resultStatusRepository;

    public TestService(TestRepository testRepository,
                       QuestionRepository questionRepository,
                       AnswerRepository answerRepository,
                       TestResultRepository testResultRepository,
                       ResultAnswerRepository resultAnswerRepository,
                       RecommendationRepository recommendationRepository,
                       StressLevelRepository stressLevelRepository,
                       ResultStatusRepository resultStatusRepository) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.testResultRepository = testResultRepository;
        this.resultAnswerRepository = resultAnswerRepository;
        this.recommendationRepository = recommendationRepository;
        this.stressLevelRepository = stressLevelRepository;
        this.resultStatusRepository = resultStatusRepository;
    }

    @Transactional(readOnly = true)
    public List<Test> findAll() {
        return testRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Test> findById(String testCode) {
        return testRepository.findById(testCode);
    }

    @Transactional
    public Test createTest(String testCode, String name, String description, String instructions) {
        Test test = new Test();
        test.setTestCode(testCode);
        test.setName(name);
        test.setDescription(description);
        test.setInstructions(instructions);
        return testRepository.save(test);
    }

    @Transactional
    public Test updateTest(String testCode, String name, String description, String instructions) {
        Test test = testRepository.findById(testCode)
                .orElseThrow(() -> new IllegalArgumentException("Тест не найден"));
        test.setName(name);
        test.setDescription(description);
        test.setInstructions(instructions);
        return testRepository.save(test);
    }

    @Transactional
    public void deleteTest(String testCode) {
        testRepository.deleteById(testCode);
    }

    @Transactional
    public void deleteQuestion(Long questionNumber) {
        questionRepository.deleteById(questionNumber);
    }

    @Transactional
    public void deleteAnswer(Long answerCode) {
        answerRepository.deleteById(answerCode);
    }

    @Transactional
    public Question addQuestion(String testCode, String questionText, Short orderNumber) {
        Test test = testRepository.findById(testCode)
                .orElseThrow(() -> new IllegalArgumentException("Тест не найден"));
        Question q = new Question();
        q.setTest(test);
        q.setQuestionText(questionText);
        q.setOrderNumber(orderNumber);
        return questionRepository.save(q);
    }

    @Transactional
    public Answer addAnswer(Long questionNumber, String answerText, Short score, Short orderNumber) {
        Question q = questionRepository.findById(questionNumber)
                .orElseThrow(() -> new IllegalArgumentException("Вопрос не найден"));
        Answer a = new Answer();
        a.setQuestion(q);
        a.setAnswerText(answerText);
        a.setScore(score);
        a.setOrderNumber(orderNumber);
        return answerRepository.save(a);
    }

    @Transactional(readOnly = true)
    public List<Question> getQuestionsByTestId(String testCode) {
        return questionRepository.findByTestCode(testCode);
    }

    @Transactional(readOnly = true)
    public List<Answer> getAnswersByQuestionId(Long questionNumber) {
        return answerRepository.findByQuestionNumber(questionNumber);
    }

    @Transactional
    public TestResult saveTestResult(Student student, Test test, Short totalScore, StressLevel stressLevel) {
        TestResult r = new TestResult();
        r.setStudent(student);
        r.setTest(test);
        r.setTakenAt(LocalDateTime.now());
        r.setTotalScore(totalScore);
        r.setStressLevel(stressLevel);
        // По умолчанию — статус «Завершён»
        ResultStatus completed = resultStatusRepository.findById("Завершён").orElse(null);
        r.setStatus(completed);
        return testResultRepository.save(r);
    }

    @Transactional(readOnly = true)
    public List<TestResult> getResultsByStudentId(Long recordBookNumber) {
        return testResultRepository.findByStudentId(recordBookNumber);
    }

    @Transactional(readOnly = true)
    public List<TestResult> getResultsByTestId(String testCode) {
        return testResultRepository.findAll().stream()
                .filter(r -> r.getTest() != null && testCode.equals(r.getTest().getTestCode()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StressLevel> getAllStressLevels() {
        return stressLevelRepository.findAll();
    }

    /**
     * Выбор уровня стресса по проценту от максимума теста.
     * Уровни сортируются по minPercent и берётся тот, в чей диапазон
     * попадает заданный процент.
     */
    @Transactional(readOnly = true)
    public StressLevel getStressLevelByPercent(int percent) {
        int p = Math.max(0, Math.min(100, percent));
        return stressLevelRepository.findAll().stream()
                .sorted((a, b) -> Short.compare(a.getMinPercent(), b.getMinPercent()))
                .filter(sl -> p >= sl.getMinPercent() && p <= sl.getMaxPercent())
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public StressLevel getStressLevelForResult(Short score, int maxScore) {
        if (score == null) return null;
        if (maxScore <= 0) return getStressLevelByPercent(0);
        int percent = Math.round((score * 100f) / maxScore);
        return getStressLevelByPercent(percent);
    }

    /**
     * Обратной совместимости: считает уровень по «абсолютному» баллу.
     * Сейчас интерпретирует балл как процент от 100. Использовать НЕ
     * рекомендуется — лучше {@link #getStressLevelForResult(Short, int)}.
     */
    @Deprecated
    @Transactional(readOnly = true)
    public StressLevel getStressLevelByScore(Short score) {
        if (score == null) return null;
        return getStressLevelByPercent(score);
    }

    /** Максимально возможный балл по тесту. */
    @Transactional(readOnly = true)
    public int getMaxPossibleScore(String testCode) {
        int total = 0;
        for (Question q : getQuestionsByTestId(testCode)) {
            int qMax = getAnswersByQuestionId(q.getQuestionNumber()).stream()
                    .map(Answer::getScore)
                    .filter(Objects::nonNull)
                    .mapToInt(Short::intValue)
                    .max()
                    .orElse(0);
            total += qMax;
        }
        return total;
    }

    @Transactional
    public StressLevel getOrCreateDefaultStressLevel(Short score) {
        StressLevel level = getStressLevelByScore(score);
        if (level != null) return level;
        StressLevel def = stressLevelRepository.findById("Не определено").orElse(null);
        if (def == null) {
            def = new StressLevel("Не определено", (short) 0, (short) 100);
            def = stressLevelRepository.save(def);
        }
        return def;
    }

    @Transactional
    public Recommendation createRecommendation(StressLevel stressLevel, String recommendationText, Short orderNumber) {
        Recommendation r = new Recommendation();
        r.setStressLevel(stressLevel);
        r.setRecommendationText(recommendationText);
        r.setOrderNumber(orderNumber);
        return recommendationRepository.save(r);
    }

    @Transactional(readOnly = true)
    public List<Recommendation> getRecommendationsByStressLevelId(String levelName) {
        return recommendationRepository.findAll().stream()
                .filter(r -> r.getStressLevel() != null
                        && levelName.equals(r.getStressLevel().getLevelName()))
                .sorted((a, b) -> {
                    Short oa = a.getOrderNumber() == null ? Short.MAX_VALUE : a.getOrderNumber();
                    Short ob = b.getOrderNumber() == null ? Short.MAX_VALUE : b.getOrderNumber();
                    return oa.compareTo(ob);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TestResult> findResultById(Long protocolNumber) {
        return testResultRepository.findById(protocolNumber);
    }

    @Transactional(readOnly = true)
    public List<Recommendation> getAllRecommendations() {
        return recommendationRepository.findAll();
    }
}
