package com.aims.backend.repository.dashboard;

import com.aims.backend.domain.dashboard.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentStatusRepository extends JpaRepository<EquipmentStatus, Long> {

    // Custom query to count the occurrences of each status
    // Returns a List of Object arrays, where each array contains [status (String), count (Long)]
    @Query("SELECT es.status, COUNT(es) FROM EquipmentStatus es GROUP BY es.status")
    List<Object[]> countAllByStatus();
}
