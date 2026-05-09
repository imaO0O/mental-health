package ru.rrtu.mental_health_system.dto;

public class QuestionDto {
    private Long id;
    private Long testId;
    private String questionText;
    private Short orderNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Short getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Short orderNumber) {
        this.orderNumber = orderNumber;
    }
}
