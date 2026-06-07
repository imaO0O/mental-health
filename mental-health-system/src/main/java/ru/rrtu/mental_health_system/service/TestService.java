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
 * Сервис тестов и протоколов тестирования (модель данных из ПЗ).
 * Заключение протокола — градация (grades.grade_name), определяемая по доле
 * набранных баллов от максимума теста.
 */
@Service
public class TestService {

    private final TestRepository testRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final TestResultRepository testResultRepository;
    private final RecommendationRepository recommendationRepository;
    private final GradeRepository gradeRepository;

    public TestService(TestRepository testRepository,
                       QuestionRepository questionRepository,
                       AnswerRepository answerRepository,
                       TestResultRepository testResultRepository,
                       RecommendationRepository recommendationRepository,
                       GradeRepository gradeRepository) {
        this.testRepository = testRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.testResultRepository = testResultRepository;
        this.recommendationRepository = recommendationRepository;
        this.gradeRepository = gradeRepository;
    }

    @Transactional(readOnly = true)
    public List<Test> findAll() { return testRepository.findAll(); }

    @Transactional(readOnly = true)
    public Optional<Test> findById(String testCode) { return testRepository.findById(testCode); }

    @Transactional
    public Test createTest(String testCode, String name, String description, String instruction) {
        Test test = new Test();
        test.setTestCode(testCode);
        test.setName(name);
        test.setDescription(description);
        test.setInstruction(instruction);
        return testRepository.save(test);
    }

    @Transactional
    public Test updateTest(String testCode, String name, String description, String instruction) {
        Test test = testRepository.findById(testCode)
                .orElseThrow(() -> new IllegalArgumentException("Тест не найден"));
        test.setName(name);
        test.setDescription(description);
        test.setInstruction(instruction);
        return testRepository.save(test);
    }

    @Transactional
    public void deleteTest(String testCode) { testRepository.deleteById(testCode); }

    @Transactional
    public void deleteQuestion(Long questionNumber) { questionRepository.deleteById(questionNumber); }

    @Transactional
    public void deleteAnswer(Long answerCode) { answerRepository.deleteById(answerCode); }

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
    public TestResult saveTestResult(Student student, Test test, Short totalScore, Grade grade) {
        TestResult r = new TestResult();
        r.setStudent(student);
        r.setTest(test);
        r.setTakenAt(LocalDateTime.now());
        r.setTotalScore(totalScore);
        r.setGrade(grade);
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

    // ---- Градации (заключения) ----

    @Transactional(readOnly = true)
    public List<Grade> getAllGrades() { return gradeRepository.findAll(); }

    /** Алиас для совместимости с прежними вызовами. */
    @Transactional(readOnly = true)
    public List<Grade> getAllStressLevels() { return gradeRepository.findAll(); }

    @Transactional(readOnly = true)
    public Grade getGradeByPercent(int percent) {
        int p = Math.max(0, Math.min(100, percent));
        return gradeRepository.findAll().stream()
                .sorted((a, b) -> Short.compare(a.getMinPercent(), b.getMinPercent()))
                .filter(g -> p >= g.getMinPercent() && p <= g.getMaxPercent())
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Grade getStressLevelForResult(Short score, int maxScore) {
        if (score == null) return null;
        if (maxScore <= 0) return getGradeByPercent(0);
        int percent = Math.round((score * 100f) / maxScore);
        return getGradeByPercent(percent);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public Grade getStressLevelByScore(Short score) {
        if (score == null) return null;
        return getGradeByPercent(score);
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
    public Grade getOrCreateDefaultGrade(Short score) {
        Grade g = getStressLevelByScore(score);
        if (g != null) return g;
        Grade def = gradeRepository.findById("Не определено").orElse(null);
        if (def == null) {
            def = gradeRepository.save(new Grade("Не определено", (short) 0, (short) 100));
        }
        return def;
    }

    @Transactional
    public Recommendation createRecommendation(Grade grade, String recommendationText, Short orderNumber) {
        Recommendation r = new Recommendation();
        r.setGrade(grade);
        r.setRecommendationText(recommendationText);
        r.setOrderNumber(orderNumber);
        return recommendationRepository.save(r);
    }

    @Transactional(readOnly = true)
    public List<Recommendation> getRecommendationsByStressLevelId(String gradeName) {
        return recommendationRepository.findAll().stream()
                .filter(r -> r.getGrade() != null && gradeName.equals(r.getGrade().getGradeName()))
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
    public List<Recommendation> getAllRecommendations() { return recommendationRepository.findAll(); }
}
