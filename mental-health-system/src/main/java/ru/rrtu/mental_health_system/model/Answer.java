package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Вариант ответа на вопрос психологического теста.
 *
 * Согласно проектировке (см. ПЗ, таблица «answers»),
 * первичный ключ — естественный сквозной код варианта ответа.
 */
@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_code")
    private Long answerCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_number", nullable = false)
    private Question question;

    @Column(name = "answer_text", nullable = false, length = 200)
    private String answerText;

    @Column(name = "score", nullable = false)
    private Short score;

    @Column(name = "order_number", nullable = false)
    private Short orderNumber;

    public Long getAnswerCode() { return answerCode; }
    public void setAnswerCode(Long c) { this.answerCode = c; }

    /** Алиас id == answerCode. */
    public Long getId() { return answerCode; }
    public void setId(Long id) { this.answerCode = id; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question q) { this.question = q; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String s) { this.answerText = s; }

    public Short getScore() { return score; }
    public void setScore(Short s) { this.score = s; }

    public Short getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Short n) { this.orderNumber = n; }
}
