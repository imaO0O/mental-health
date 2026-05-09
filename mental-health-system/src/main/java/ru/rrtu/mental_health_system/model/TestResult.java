package ru.rrtu.mental_health_system.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Протокол прохождения теста (test_protocols).
 *
 * Согласно проектировке (см. ПЗ, таблица «test_protocols»),
 * первичный ключ — естественный «Номер протокола тестирования».
 *
 * Класс назван TestResult по историческим причинам, но соответствует
 * сущности «Протокол тестирования» из ПЗ.
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_name")
    private StressLevel stressLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_name")
    private ResultStatus status;

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

    public StressLevel getStressLevel() { return stressLevel; }
    public void setStressLevel(StressLevel sl) { this.stressLevel = sl; }

    public ResultStatus getStatus() { return status; }
    public void setStatus(ResultStatus s) { this.status = s; }
}
