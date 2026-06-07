package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Результат по пункту теста (таблица «item_results»).
 *
 * Слабая сущность связи «Протокол × Вопрос» (N:M). Составной первичный ключ —
 * (Номер протокола + Номер вопроса), оба являются внешними ключами.
 * Единственный неключевой атрибут — выбранный студентом вариант ответа.
 */
@Entity
@Table(name = "item_results")
@IdClass(ItemResultId.class)
public class ItemResult {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_number", nullable = false)
    private TestResult protocol;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_number", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_code", nullable = false)
    private Answer answer;

    public ItemResult() {}

    public ItemResult(TestResult protocol, Question question, Answer answer) {
        this.protocol = protocol;
        this.question = question;
        this.answer = answer;
    }

    public TestResult getProtocol() { return protocol; }
    public void setProtocol(TestResult p) { this.protocol = p; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question q) { this.question = q; }

    public Answer getAnswer() { return answer; }
    public void setAnswer(Answer a) { this.answer = a; }
}
