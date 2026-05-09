package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Статус прохождения теста: Завершён, Прерван, Аннулирован,
 * Требует консультации.
 *
 * Согласно проектировке (см. ПЗ, таблица «result_statuses»),
 * первичный ключ — естественный: название статуса.
 */
@Entity
@Table(name = "result_statuses")
public class ResultStatus {

    @Id
    @Column(name = "status_name", length = 30, nullable = false)
    private String statusName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public ResultStatus() {}

    public ResultStatus(String statusName, String description) {
        this.statusName = statusName;
        this.description = description;
    }

    public String getStatusName() { return statusName; }
    public void setStatusName(String statusName) { this.statusName = statusName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
