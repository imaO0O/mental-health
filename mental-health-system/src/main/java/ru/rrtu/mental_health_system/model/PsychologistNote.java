package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Клиническая заметка психолога по студенту.
 *
 * Соответствует слабой сущности «Заметки психолога» из ПЗ —
 * результат преобразования связи «Курирует» (M:N) между
 * Психологами и Студентами.
 *
 * Первичный ключ — естественный «Номер заметки» (note_number).
 */
@Entity
@Table(name = "psychologist_notes")
public class PsychologistNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_number")
    private Long noteNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personnel_number", nullable = false)
    private Psychologist psychologist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_book_number", nullable = false)
    private Student student;

    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    @Column(name = "note_text", nullable = false, columnDefinition = "TEXT")
    private String noteText;

    @PrePersist
    protected void onCreate() {
        if (noteDate == null) noteDate = LocalDate.now();
    }

    public Long getNoteNumber() { return noteNumber; }
    public void setNoteNumber(Long noteNumber) { this.noteNumber = noteNumber; }

    public Psychologist getPsychologist() { return psychologist; }
    public void setPsychologist(Psychologist psychologist) { this.psychologist = psychologist; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public LocalDate getNoteDate() { return noteDate; }
    public void setNoteDate(LocalDate noteDate) { this.noteDate = noteDate; }

    public String getNoteText() { return noteText; }
    public void setNoteText(String noteText) { this.noteText = noteText; }
}
