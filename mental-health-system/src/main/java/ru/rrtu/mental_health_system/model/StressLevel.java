package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Уровень стресса.
 *
 * Согласно проектировке (см. ПЗ, таблица «stress_levels»),
 * первичный ключ — естественный: название уровня
 * (Низкий, Средний, Высокий, Критический).
 *
 * Границы хранятся в процентах от максимума теста (0..100).
 */
@Entity
@Table(name = "stress_levels")
public class StressLevel {

    @Id
    @Column(name = "level_name", length = 30, nullable = false)
    private String levelName;

    @Column(name = "min_percent", nullable = false)
    private Short minPercent;

    @Column(name = "max_percent", nullable = false)
    private Short maxPercent;

    public StressLevel() {}

    public StressLevel(String levelName, Short minPercent, Short maxPercent) {
        this.levelName = levelName;
        this.minPercent = minPercent;
        this.maxPercent = maxPercent;
    }

    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }

    public Short getMinPercent() { return minPercent; }
    public void setMinPercent(Short minPercent) { this.minPercent = minPercent; }

    public Short getMaxPercent() { return maxPercent; }
    public void setMaxPercent(Short maxPercent) { this.maxPercent = maxPercent; }

    /** Алиасы для обратной совместимости с существующими шаблонами/кодом. */
    public String getName() { return levelName; }
    public void setName(String n) { this.levelName = n; }
    public String getId() { return levelName; }
    public void setId(String id) { this.levelName = id; }

    public Short getMinScore() { return minPercent; }
    public Short getMaxScore() { return maxPercent; }
    public void setMinScore(Short s) { this.minPercent = s; }
    public void setMaxScore(Short s) { this.maxPercent = s; }
    public void setMinScore(short s) { this.minPercent = s; }
    public void setMaxScore(short s) { this.maxPercent = s; }
}
