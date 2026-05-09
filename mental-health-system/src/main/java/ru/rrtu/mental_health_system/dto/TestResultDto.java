package ru.rrtu.mental_health_system.dto;

public class TestResultDto {
    private Long id;
    private String testName;
    private Short totalScore;
    private String stressLevelName;
    private String dateTaken;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public Short getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(Short totalScore) {
        this.totalScore = totalScore;
    }

    public String getStressLevelName() {
        return stressLevelName;
    }

    public void setStressLevelName(String stressLevelName) {
        this.stressLevelName = stressLevelName;
    }

    public String getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(String dateTaken) {
        this.dateTaken = dateTaken;
    }
}
