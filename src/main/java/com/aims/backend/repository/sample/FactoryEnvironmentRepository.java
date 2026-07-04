package com.aims.backend.repository.sample;

import com.aims.backend.domain.dashboard.FactoryEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FactoryEnvironmentRepository extends JpaRepository<FactoryEnvironment, Long> {

    @Query("SELECT fe FROM FactoryEnvironment fe WHERE fe.createdAt = :createdAt")
    List<FactoryEnvironment> findByCreatedAt(@Param("createdAt") LocalDateTime createdAt);

}
