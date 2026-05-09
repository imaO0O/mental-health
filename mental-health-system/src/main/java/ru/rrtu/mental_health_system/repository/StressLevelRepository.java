package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.StressLevel;

import java.util.Optional;

public interface StressLevelRepository extends JpaRepository<StressLevel, String> {
    default Optional<StressLevel> findByName(String name) { return findById(name); }
}
