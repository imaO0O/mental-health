package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Вопрос психологического теста.
 *
 * Согласно проектировке (см. ПЗ, таблица «questions»),
 * первичный ключ — естественный сквозной номер вопроса.
 */
@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_number")
    private Long questionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_code", nullable = false)
    private Test test;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "order_number", nullable = false)
    private Short orderNumber;

    public Long getQuestionNumber() { return questionNumber; }
    public void setQuestionNumber(Long n) { this.questionNumber = n; }

    /** Алиас id == questionNumber. */
    public Long getId() { return questionNumber; }
    public void setId(Long id) { this.questionNumber = id; }

    public Test getTest() { return test; }
    public void setTest(Test test) { this.test = test; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String s) { this.questionText = s; }

    public Short getOrderNumber() { return orderNumber; }
    public void setOrderNumber(Short n) { this.orderNumber = n; }
}
