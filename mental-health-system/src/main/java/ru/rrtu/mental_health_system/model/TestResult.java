package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Протокол прохождения теста (таблица «test_protocols»).
 *
 * Первичный ключ — естественный «Номер протокола». Заключение протокола —
 * градация (grade_name), определяемая по доле баллов от максимума.
 * Класс назван TestResult по историческим причинам.
 */
@Entity
@Table(name = "test_protocols")
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "protocol_number")
    private Long protocolNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_book_number", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_code", nullable = false)
    private Test test;

    @Column(name = "taken_at", nullable = false)
    private LocalDateTime takenAt;

    @Column(name = "total_score", nullable = false)
    private Short totalScore;

    /** Заключение — градация (FK на grade_name). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_name")
    private Grade grade;

    @PrePersist
    protected void onCreate() {
        if (takenAt == null) takenAt = LocalDateTime.now();
    }

    public Long getProtocolNumber() { return protocolNumber; }
    public void setProtocolNumber(Long n) { this.protocolNumber = n; }

    /** Алиас id == protocolNumber. */
    public Long getId() { return protocolNumber; }
    public void setId(Long id) { this.protocolNumber = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student s) { this.student = s; }

    public Test getTest() { return test; }
    public void setTest(Test t) { this.test = t; }

    public LocalDateTime getTakenAt() { return takenAt; }
    public void setTakenAt(LocalDateTime t) { this.takenAt = t; }

    /** Дата прохождения (для совместимости со старыми шаблонами). */
    public java.time.LocalDate getDateTaken() {
        return takenAt != null ? takenAt.toLocalDate() : null;
    }
    public void setDateTaken(java.time.LocalDate d) {
        if (d != null) this.takenAt = d.atStartOfDay();
    }

    public Short getTotalScore() { return totalScore; }
    public void setTotalScore(Short s) { this.totalScore = s; }

    public Grade getGrade() { return grade; }
    public void setGrade(Grade g) { this.grade = g; }

    /** Алиасы для совместимости со старыми шаблонами (stressLevel). */
    public Grade getStressLevel() { return grade; }
    public void setStressLevel(Grade g) { this.grade = g; }
}
