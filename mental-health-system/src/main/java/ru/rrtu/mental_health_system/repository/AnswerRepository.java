package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rrtu.mental_health_system.model.Answer;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    @Query("SELECT a FROM Answer a WHERE a.question.questionNumber = :qn ORDER BY a.orderNumber")
    List<Answer> findByQuestionNumber(@Param("qn") Long questionNumber);
}
