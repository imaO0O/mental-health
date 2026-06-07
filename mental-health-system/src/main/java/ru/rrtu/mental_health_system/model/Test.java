package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Психологический тест (методика) — таблица «tests».
 * Первичный ключ — шифр теста. Внешние ключи: автор-психолог и
 * измеряемый показатель (indicator_name).
 */
@Entity
@Table(name = "tests")
public class Test {

    @Id
    @Column(name = "test_code", length = 20, nullable = false)
    private String testCode;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "instruction", columnDefinition = "TEXT")
    private String instruction;

    @Column(name = "is_active")
    private Boolean isActive;

    /** Психолог-автор теста (FK на personnel_number). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_personnel_number")
    private Psychologist author;

    /** Измеряемый показатель (FK на indicator_name). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_name")
    private Indicator indicator;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) isActive = true;
    }

    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }

    /** Алиас «id» == testCode. */
    public String getId() { return testCode; }
    public void setId(String id) { this.testCode = id; }

    public String getName() { return name; }
    public void setName(String s) { this.name = s; }

    public String getDescription() { return description; }
    public void setDescription(String s) { this.description = s; }

    public String getInstruction() { return instruction; }
    public void setInstruction(String s) { this.instruction = s; }

    /** Алиасы для совместимости (instructions). */
    public String getInstructions() { return instruction; }
    public void setInstructions(String s) { this.instruction = s; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean a) { this.isActive = a; }

    public Psychologist getAuthor() { return author; }
    public void setAuthor(Psychologist a) { this.author = a; }

    public Indicator getIndicator() { return indicator; }
    public void setIndicator(Indicator i) { this.indicator = i; }

    /** Алиасы для совместимости (category). */
    public Indicator getCategory() { return indicator; }
    public void setCategory(Indicator i) { this.indicator = i; }
}
