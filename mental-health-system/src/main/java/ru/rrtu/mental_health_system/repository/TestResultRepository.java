package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.TestResult;

import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    /** Поиск протоколов по студенту (по номеру зачётной книжки). */
    @org.springframework.data.jpa.repository.Query(
        "SELECT r FROM TestResult r WHERE r.student.recordBookNumber = :rbn"
    )
    List<TestResult> findByStudentId(@org.springframework.data.repository.query.Param("rbn") Long recordBookNumber);
}
