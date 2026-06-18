package com.aims.backend.repository.dashboard;

import com.aims.backend.domain.dashboard.EquipmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentStatusRepository extends JpaRepository<EquipmentStatusHistory, String> {

    @Query("SELECT esh.operationStatus, COUNT(esh) FROM EquipmentStatusHistory esh GROUP BY esh.operationStatus")
    List<Object[]> countAllByStatus();
}
