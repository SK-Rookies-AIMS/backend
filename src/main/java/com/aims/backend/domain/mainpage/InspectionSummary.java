package com.aims.backend.domain.mainpage;

import com.aims.backend.domain.commons.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "inspection_summary")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InspectionSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total_count")
    private Integer totalCount;

    @Column(name = "normal_count")
    private Integer normalCount;

    @Column(name = "normal_rate")
    private Double normalRate;

    @Column(name = "abnormal_count")
    private Integer abnormalCount;

    @Column(name = "abnormal_rate")
    private Double abnormalRate;

    @Column(name = "standby_count")
    private Integer standbyCount;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}