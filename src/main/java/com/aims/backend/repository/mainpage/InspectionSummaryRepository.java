package com.aims.backend.repository.mainpage;

import com.aims.backend.domain.mainpage.InspectionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InspectionSummaryRepository extends JpaRepository<InspectionSummary, Long> {

    @Query("SELECT i FROM InspectionSummary i WHERE i.createdAt BETWEEN :startDate AND :endDate AND i.totalCount = :totalCount")
    List<InspectionSummary> findByCreatedAtBetweenAndTotalCount(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("totalCount") Integer totalCount
    );
}
