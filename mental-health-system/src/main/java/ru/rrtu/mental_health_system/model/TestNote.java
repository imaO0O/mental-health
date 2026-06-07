package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Заметка по тестированию (таблица «test_notes») — датированное наблюдение
 * психолога по конкретному протоколу тестирования. Слабая сущность связи
 * «Комментирует» (Психолог × Протокол тестирования).
 */
@Entity
@Table(name = "test_notes")
public class TestNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_number")
    private Long noteNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personnel_number", nullable = false)
    private Psychologist psychologist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_number", nullable = false)
    private TestResult protocol;

    @Column(name = "note_date", nullable = false)
    private LocalDate noteDate;

    @Column(name = "note_text", nullable = false, columnDefinition = "TEXT")
    private String noteText;

    @PrePersist
    protected void onCreate() {
        if (noteDate == null) noteDate = LocalDate.now();
    }

    public Long getNoteNumber() { return noteNumber; }
    public void setNoteNumber(Long n) { this.noteNumber = n; }
    public Long getId() { return noteNumber; }

    public Psychologist getPsychologist() { return psychologist; }
    public void setPsychologist(Psychologist p) { this.psychologist = p; }

    public TestResult getProtocol() { return protocol; }
    public void setProtocol(TestResult p) { this.protocol = p; }

    public LocalDate getNoteDate() { return noteDate; }
    public void setNoteDate(LocalDate d) { this.noteDate = d; }

    public String getNoteText() { return noteText; }
    public void setNoteText(String s) { this.noteText = s; }
}
