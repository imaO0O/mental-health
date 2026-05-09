package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Категория психологического теста (Стресс, Тревога, Депрессия,
 * Эмоциональное выгорание и т. п.).
 *
 * Согласно проектировке (см. ПЗ, таблица «test_categories»),
 * первичный ключ — естественный: название категории.
 */
@Entity
@Table(name = "test_categories")
public class TestCategory {

    @Id
    @Column(name = "category_name", length = 50, nullable = false)
    private String categoryName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public TestCategory() {}

    public TestCategory(String categoryName, String description) {
        this.categoryName = categoryName;
        this.description = description;
    }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
