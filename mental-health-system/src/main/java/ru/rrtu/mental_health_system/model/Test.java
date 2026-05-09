package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Психологический тест.
 *
 * Согласно проектировке (см. ПЗ, таблица «tests»),
 * первичный ключ — естественный: шифр теста (PSS-10, BDI, STAI, BAI и т. д.).
 * Дополнительные внешние ключи: автор (психолог) и категория тестов.
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

    @Column(name = "instructions", columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "is_active")
    private Boolean isActive;

    /** Психолог-автор теста (FK на personnel_number). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_personnel_number")
    private Psychologist author;

    /** Категория теста (FK на category_name). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_name")
    private TestCategory category;

    @PrePersist
    protected void onCreate() {
        if (isActive == null) isActive = true;
    }

    public String getTestCode() { return testCode; }
    public void setTestCode(String testCode) { this.testCode = testCode; }

    /** Алиас «id» == testCode для обратной совместимости. */
    public String getId() { return testCode; }
    public void setId(String id) { this.testCode = id; }

    public String getName() { return name; }
    public void setName(String s) { this.name = s; }

    public String getDescription() { return description; }
    public void setDescription(String s) { this.description = s; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String s) { this.instructions = s; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean a) { this.isActive = a; }

    public Psychologist getAuthor() { return author; }
    public void setAuthor(Psychologist a) { this.author = a; }

    public TestCategory getCategory() { return category; }
    public void setCategory(TestCategory c) { this.category = c; }
}
