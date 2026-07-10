package com.aims.backend.repository.dashboard;

import com.aims.backend.domain.dashboard.AgvOperation;
import com.aims.backend.domain.dashboard.enums.AgvStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgvOperationRepository extends JpaRepository<AgvOperation, Long> {

    long countByAgvStatus(AgvStatus agvStatus);

    List<AgvOperation> findByAgvStatusIn(List<AgvStatus> statuses);

    Optional<AgvOperation> findFirstByRouteCodeAndAgvStatusOrderByLaneNoAsc(
            String routeCode,
            AgvStatus agvStatus
    );

    @Query(
            value = """
                    SELECT *
                    FROM agv_operation
                    WHERE route_code = :routeCode
                      AND agv_status = :agvStatus
                    ORDER BY lane_no ASC
                    LIMIT 1
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    Optional<AgvOperation> findFirstWaitingAgvForUpdateSkipLocked(
            @Param("routeCode") String routeCode,
            @Param("agvStatus") String agvStatus
    );
}
