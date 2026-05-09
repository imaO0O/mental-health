package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.ResultStatus;

public interface ResultStatusRepository extends JpaRepository<ResultStatus, String> {
}
