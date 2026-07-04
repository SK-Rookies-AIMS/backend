package com.aims.backend.repository.dashboard;

import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import com.aims.backend.dto.dashboard.AgvStatusCountResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import java.util.List;
import java.util.Optional;

public interface AgvOperationRepository extends JpaRepository<AgvOperation, Long> {

    long countByAgvStatus(AgvStatus agvStatus);

    List<AgvOperation> findByAgvStatusIn(List<AgvStatus> statuses);

    Optional<AgvOperation> findFirstByRouteCodeAndAgvStatusOrderByLaneNoAsc(
            String routeCode,
            AgvStatus agvStatus
    );
}