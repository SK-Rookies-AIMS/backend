package com.aims.backend.domain.alert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.aims.backend.domain.user.UserRole;

import java.time.LocalDateTime;

import com.aims.backend.domain.alert.ActionCategory;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "action_timeline")
public class ActionTimeline {
    
    @Id
    @Column(name = "action_id", nullable = false, length = 20)
    private String actionId;

    @Column(name = "log_no", nullable = false, length = 20)
    private String logNo;

    @Column(name = "emp_no", nullable = false, length = 20)
    private String empNo;

    @Column(name = "emp_name", nullable = false, length = 20)
    private String empName;

    @Enumerated(EnumType.STRING)
    @Column(name = "emp_role", nullable = false, length = 20)
    private UserRole empRole;

    @Column(name = "action_time", nullable = false)
    private LocalDateTime actionTime;

    @Column(name = "action_content", nullable = false, length = 20)
    private String actionContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_category", nullable = false, length = 20)
    private ActionCategory actionCategory;

    @Column(name = "action_result", nullable = false, length = 20)
    private String actionResult;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}