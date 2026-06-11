package com.aims.backend.repository;

import com.aims.backend.domain.mainpage.UserTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserTaskRepository extends JpaRepository<UserTask, Long> {

    List<UserTask> findTop3ByUserIdAndScheduledAtGreaterThanEqualOrderByScheduledAtAsc(Long userId,LocalDateTime scheduledAt);
}
