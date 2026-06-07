package ru.rrtu.mental_health_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.rrtu.mental_health_system.model.ItemResult;
import ru.rrtu.mental_health_system.model.ItemResultId;

import java.util.List;

public interface ItemResultRepository extends JpaRepository<ItemResult, ItemResultId> {
    @Query("SELECT ir FROM ItemResult ir WHERE ir.protocol.protocolNumber = :pn")
    List<ItemResult> findByProtocol(@Param("pn") Long protocolNumber);
}
