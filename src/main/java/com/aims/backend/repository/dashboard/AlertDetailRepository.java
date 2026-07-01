package com.aims.backend.repository.dashboard;

import com.aims.backend.domain.dashboard.AlertDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertDetailRepository extends JpaRepository<AlertDetail, Long>, QuerydslPredicateExecutor<AlertDetail> {
}
