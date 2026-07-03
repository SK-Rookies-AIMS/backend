package com.aims.backend.repository.alert;

import com.aims.backend.domain.alert.AlertEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlertEventRepository extends JpaRepository<AlertEvent, String> {

    boolean existsByEventId(String eventId);

    Optional<AlertEvent> findByEventId(String eventId);
}
