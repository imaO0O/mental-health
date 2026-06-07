package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rrtu.mental_health_system.model.TestNote;

import java.util.List;

public interface TestNoteRepository extends JpaRepository<TestNote, Long> {

    @Query("SELECT n FROM TestNote n WHERE n.protocol.protocolNumber = :pn ORDER BY n.noteDate DESC")
    List<TestNote> findByProtocol(@Param("pn") Long protocolNumber);

    /** Все заметки по протоколам конкретного студента. */
    @Query("SELECT n FROM TestNote n WHERE n.protocol.student.recordBookNumber = :rbn ORDER BY n.noteDate DESC")
    List<TestNote> findByStudent(@Param("rbn") Long recordBookNumber);

    @Query("SELECT n FROM TestNote n WHERE n.psychologist.personnelNumber = :pn ORDER BY n.noteDate DESC")
    List<TestNote> findByPsychologist(@Param("pn") Long personnelNumber);
}
