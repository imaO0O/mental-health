package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rrtu.mental_health_system.model.Psychologist;

public interface PsychologistRepository extends JpaRepository<Psychologist, Long> {

    @Query("SELECT p FROM Psychologist p WHERE p.user.login = :login")
    Psychologist findByLogin(@Param("login") String login);

    /** Backward-compatible alias. */
    default Psychologist findByUserId(String login) { return findByLogin(login); }
}
