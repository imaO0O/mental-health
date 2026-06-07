package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Градация выраженности показателя (Низкий, Средний, Высокий, Критический).
 *
 * Согласно ПЗ (таблица «grades»), первичный ключ — естественное название
 * градации. Диапазон [min_percent; max_percent] задаёт долю набранных баллов
 * от максимума, по которой определяется заключение протокола.
 */
@Entity
@Table(name = "grades")
public class Grade {

    @Id
    @Column(name = "grade_name", length = 30, nullable = false)
    private String gradeName;

    @Column(name = "min_percent", nullable = false)
    private Short minPercent;

    @Column(name = "max_percent", nullable = false)
    private Short maxPercent;

    public Grade() {}

    public Grade(String gradeName, Short minPercent, Short maxPercent) {
        this.gradeName = gradeName;
        this.minPercent = minPercent;
        this.maxPercent = maxPercent;
    }

    public String getGradeName() { return gradeName; }
    public void setGradeName(String s) { this.gradeName = s; }

    /** Алиасы для совместимости с шаблонами/контроллерами. */
    public String getName() { return gradeName; }
    public String getLevelName() { return gradeName; }
    public String getId() { return gradeName; }
    public void setId(String id) { this.gradeName = id; }

    public Short getMinPercent() { return minPercent; }
    public void setMinPercent(Short v) { this.minPercent = v; }

    public Short getMaxPercent() { return maxPercent; }
    public void setMaxPercent(Short v) { this.maxPercent = v; }

    /** Алиасы для совместимости с шаблонами (minScore/maxScore). */
    public Short getMinScore() { return minPercent; }
    public Short getMaxScore() { return maxPercent; }
}
