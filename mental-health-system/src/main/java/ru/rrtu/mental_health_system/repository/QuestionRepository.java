package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rrtu.mental_health_system.model.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    @Query("SELECT q FROM Question q WHERE q.test.testCode = :code ORDER BY q.orderNumber")
    List<Question> findByTestCode(@Param("code") String testCode);
}
