package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Консультация (таблица «consultations») — датированная личная беседа
 * психолога со студентом. Слабая сущность связи «Консультирует» (M:N).
 */
@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consultation_number")
    private Long consultationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personnel_number", nullable = false)
    private Psychologist psychologist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_book_number", nullable = false)
    private Student student;

    @Column(name = "consultation_date", nullable = false)
    private LocalDate consultationDate;

    @Column(name = "consultation_text", columnDefinition = "TEXT")
    private String consultationText;

    @PrePersist
    protected void onCreate() {
        if (consultationDate == null) consultationDate = LocalDate.now();
    }

    public Long getConsultationNumber() { return consultationNumber; }
    public void setConsultationNumber(Long n) { this.consultationNumber = n; }
    public Long getId() { return consultationNumber; }

    public Psychologist getPsychologist() { return psychologist; }
    public void setPsychologist(Psychologist p) { this.psychologist = p; }

    public Student getStudent() { return student; }
    public void setStudent(Student s) { this.student = s; }

    public LocalDate getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDate d) { this.consultationDate = d; }

    public String getConsultationText() { return consultationText; }
    public void setConsultationText(String s) { this.consultationText = s; }
}
