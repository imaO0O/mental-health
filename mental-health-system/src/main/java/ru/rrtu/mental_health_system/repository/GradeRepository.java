package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.Grade;

import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, String> {
    default Optional<Grade> findByName(String name) { return findById(name); }
}
