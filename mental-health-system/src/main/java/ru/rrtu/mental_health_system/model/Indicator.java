package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Показатель психодиагностики (Стресс, Тревога, Депрессия, Эмоциональное
 * выгорание). Согласно ПЗ (таблица «indicators») задаёт измеряемую
 * характеристику теста и специализацию психологов.
 */
@Entity
@Table(name = "indicators")
public class Indicator {

    @Id
    @Column(name = "indicator_name", length = 50, nullable = false)
    private String indicatorName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public Indicator() {}

    public Indicator(String indicatorName, String description) {
        this.indicatorName = indicatorName;
        this.description = description;
    }

    public String getIndicatorName() { return indicatorName; }
    public void setIndicatorName(String s) { this.indicatorName = s; }

    /** Алиасы для совместимости. */
    public String getName() { return indicatorName; }
    public String getCategoryName() { return indicatorName; }
    public String getId() { return indicatorName; }
    public void setId(String id) { this.indicatorName = id; }

    public String getDescription() { return description; }
    public void setDescription(String s) { this.description = s; }
}
