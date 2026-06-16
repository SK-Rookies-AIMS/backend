package com.aims.backend.repository.dashboard;

import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.dto.dashboard.AgvStatusCountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AgvOperationRepository
        extends JpaRepository<AgvOperation, Long> {

    long countByAgvStatus(AgvStatus agvStatus);

    @Query("SELECT new com.aims.backend.dto.dashboard.AgvStatusCountResponse(ao.agvStatus, COUNT(ao.id)) FROM AgvOperation ao GROUP BY ao.agvStatus")
}