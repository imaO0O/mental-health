package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.Recommendation;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
}
