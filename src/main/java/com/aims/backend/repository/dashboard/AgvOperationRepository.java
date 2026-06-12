package com.aims.backend.repository.dashboard;

import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgvOperationRepository
        extends JpaRepository<AgvOperation, Long> {

    long countByAgvStatus(AgvStatus agvStatus);
}