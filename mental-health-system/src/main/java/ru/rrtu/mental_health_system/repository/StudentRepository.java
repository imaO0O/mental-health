package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rrtu.mental_health_system.model.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s WHERE s.user.login = :login")
    Student findByLogin(@Param("login") String login);

    /** Backward-compatible alias: ранее называлось findByUserId. */
    default Student findByUserId(String login) { return findByLogin(login); }

    @Override
    List<Student> findAll();
}
