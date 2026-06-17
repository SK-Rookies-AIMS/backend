package com.aims.backend.repository.dashboard;

import com.aims.backend.domain.dashboard.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentStatusRepository extends JpaRepository<EquipmentStatus, Long> {

    @Query("SELECT es.status, COUNT(es) FROM EquipmentStatus es GROUP BY es.status")
    List<Object[]> countAllByStatus();
}
