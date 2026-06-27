package com.aims.backend.repository.sample;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aims.backend.domain.dashboard.Equipment;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    @Query("SELECT e.currentStatus, COUNT(e) FROM Equipment e GROUP BY e.currentStatus")
    List<Object[]> countAllByCurrentStatus();
}
