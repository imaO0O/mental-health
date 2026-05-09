package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;

/**
 * Специализация психолога на категории тестов.
 *
 * Junction-таблица для связи M:N «Специализируется» между
 * Психологами и Категориями тестов. Первичный ключ — составной
 * (Табельный номер + Название категории), как в ПЗ образца
 * (manager_specializations).
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
    @JoinColumn(name = "category_name", nullable = false)
    private TestCategory category;

    public PsychologistSpecialization() {}

    public PsychologistSpecialization(Psychologist psychologist, TestCategory category) {
        this.psychologist = psychologist;
        this.category = category;
    }

    public Psychologist getPsychologist() { return psychologist; }
    public void setPsychologist(Psychologist psychologist) { this.psychologist = psychologist; }

    public TestCategory getCategory() { return category; }
    public void setCategory(TestCategory category) { this.category = category; }
}
