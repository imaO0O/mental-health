package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.TestCategory;

public interface TestCategoryRepository extends JpaRepository<TestCategory, String> {
}
