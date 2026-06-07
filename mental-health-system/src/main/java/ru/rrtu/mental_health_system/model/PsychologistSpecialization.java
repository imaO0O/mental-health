package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Специализация психолога на показателе (таблица «psychologist_specializations»).
 * Junction-таблица связи M:N «Специализируется» между Психологами и
 * Показателями. Первичный ключ — составной (Табельный номер + Название показателя).
 */
@Entity
@Table(name = "psychologist_specializations")
@IdClass(PsychologistSpecializationId.class)
public class PsychologistSpecialization {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personnel_number", nullable = false)
    private Psychologist psychologist;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_name", nullable = false)
    private Indicator indicator;

    public PsychologistSpecialization() {}

    public PsychologistSpecialization(Psychologist psychologist, Indicator indicator) {
        this.psychologist = psychologist;
        this.indicator = indicator;
    }

    public Psychologist getPsychologist() { return psychologist; }
    public void setPsychologist(Psychologist psychologist) { this.psychologist = psychologist; }

    public Indicator getIndicator() { return indicator; }
    public void setIndicator(Indicator indicator) { this.indicator = indicator; }

    /** Алиасы для совместимости (category). */
    public Indicator getCategory() { return indicator; }
    public void setCategory(Indicator indicator) { this.indicator = indicator; }
}
