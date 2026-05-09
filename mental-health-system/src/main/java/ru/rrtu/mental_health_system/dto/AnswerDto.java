package ru.rrtu.mental_health_system.dto;

public class AnswerDto {
    private Long id;
    private Long questionId;
    private String answerText;
    private Short score;
    private Short orderNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Short getScore() {
        return score;
    }

    public void setScore(Short score) {
        this.score = score;
    }

    public Short getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Short orderNumber) {
        this.orderNumber = orderNumber;
    }
}
