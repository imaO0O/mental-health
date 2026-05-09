package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.Test;

public interface TestRepository extends JpaRepository<Test, String> {
}
