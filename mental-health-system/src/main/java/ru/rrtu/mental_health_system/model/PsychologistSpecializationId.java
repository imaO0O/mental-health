package ru.rrtu.mental_health_system.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Составной первичный ключ junction-таблицы «Специализации психологов»:
 * (Табельный номер психолога, Название показателя).
 */
public class PsychologistSpecializationId implements Serializable {

    private Long psychologist;   // personnel_number
    private String indicator;    // indicator_name

    public PsychologistSpecializationId() {}

    public PsychologistSpecializationId(Long psychologist, String indicator) {
        this.psychologist = psychologist;
        this.indicator = indicator;
    }

    public Long getPsychologist() { return psychologist; }
    public void setPsychologist(Long psychologist) { this.psychologist = psychologist; }

    public String getIndicator() { return indicator; }
    public void setIndicator(String indicator) { this.indicator = indicator; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PsychologistSpecializationId)) return false;
        PsychologistSpecializationId that = (PsychologistSpecializationId) o;
        return Objects.equals(psychologist, that.psychologist)
                && Objects.equals(indicator, that.indicator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(psychologist, indicator);
    }
}
