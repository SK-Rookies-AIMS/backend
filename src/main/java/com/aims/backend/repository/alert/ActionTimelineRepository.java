package com.aims.backend.repository.alert;

import org.springframework.stereotype.Repository;

import com.aims.backend.domain.alert.ActionTimeline;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


@Repository
public interface ActionTimelineRepository extends JpaRepository<ActionTimeline, String> {
    List<ActionTimeline> findByLogNoOrderByActionTimeAsc(String logNo);
}
