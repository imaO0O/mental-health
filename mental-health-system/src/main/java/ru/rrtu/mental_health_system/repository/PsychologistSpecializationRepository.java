package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rrtu.mental_health_system.model.PsychologistSpecialization;
import ru.rrtu.mental_health_system.model.PsychologistSpecializationId;

public interface PsychologistSpecializationRepository
        extends JpaRepository<PsychologistSpecialization, PsychologistSpecializationId> {
}
