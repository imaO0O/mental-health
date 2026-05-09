package ru.rrtu.mental_health_system.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Композитный первичный ключ для junction-таблицы «Специализации психологов»:
 * (Табельный номер психолога, Название категории тестов).
 *
 * Это единственная таблица в проекте с составным PK — по аналогии с
 * manager_specializations из образца ПЗ.
 */
public class PsychologistSpecializationId implements Serializable {

    private Long psychologist;       // personnel_number
    private String category;         // category_name

    public PsychologistSpecializationId() {}

    public PsychologistSpecializationId(Long psychologist, String category) {
        this.psychologist = psychologist;
        this.category = category;
    }

    public Long getPsychologist() { return psychologist; }
    public void setPsychologist(Long psychologist) { this.psychologist = psychologist; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PsychologistSpecializationId)) return false;
        PsychologistSpecializationId that = (PsychologistSpecializationId) o;
        return Objects.equals(psychologist, that.psychologist)
                && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psychologist, category);
    }
}
