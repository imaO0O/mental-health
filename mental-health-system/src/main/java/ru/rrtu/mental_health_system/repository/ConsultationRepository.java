package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rrtu.mental_health_system.model.Consultation;

import java.time.LocalDate;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    @Query("SELECT c FROM Consultation c WHERE c.student.recordBookNumber = :rbn ORDER BY c.consultationDate DESC")
    List<Consultation> findByStudent(@Param("rbn") Long recordBookNumber);

    @Query("SELECT c FROM Consultation c WHERE c.psychologist.personnelNumber = :pn ORDER BY c.consultationDate DESC")
    List<Consultation> findByPsychologist(@Param("pn") Long personnelNumber);

    /** Дата последней консультации студента (NULL — консультаций не было). */
    @Query("SELECT MAX(c.consultationDate) FROM Consultation c WHERE c.student.recordBookNumber = :rbn")
    LocalDate findLastDate(@Param("rbn") Long recordBookNumber);
}
