package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rrtu.mental_health_system.model.PsychologistNote;

import java.util.List;

public interface PsychologistNoteRepository extends JpaRepository<PsychologistNote, Long> {
    @Query("SELECT n FROM PsychologistNote n WHERE n.student.recordBookNumber = :rbn ORDER BY n.noteDate DESC")
    List<PsychologistNote> findByStudent(@Param("rbn") Long recordBookNumber);

    @Query("SELECT n FROM PsychologistNote n WHERE n.psychologist.personnelNumber = :pn ORDER BY n.noteDate DESC")
    List<PsychologistNote> findByPsychologist(@Param("pn") Long personnelNumber);
}
