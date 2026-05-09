package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Запись ответа пользователя на конкретный вопрос в рамках протокола.
 *
 * Согласно проектировке (см. ПЗ, таблица «result_answers»),
 * первичный ключ — естественный сквозной номер записи ответа.
 */
@Entity
@Table(name = "result_answers")
public class ResultAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "response_number")
    private Long responseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_number", nullable = false)
    private TestResult testResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_number", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_code", nullable = false)
    private Answer answer;

    public Long getResponseNumber() { return responseNumber; }
    public void setResponseNumber(Long n) { this.responseNumber = n; }

    /** Алиас id == responseNumber. */
    public Long getId() { return responseNumber; }
    public void setId(Long id) { this.responseNumber = id; }

    public TestResult getTestResult() { return testResult; }
    public void setTestResult(TestResult t) { this.testResult = t; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question q) { this.question = q; }

    public Answer getAnswer() { return answer; }
    public void setAnswer(Answer a) { this.answer = a; }
}
