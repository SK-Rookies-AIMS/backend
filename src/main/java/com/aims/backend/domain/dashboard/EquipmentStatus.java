package com.aims.backend.domain.dashboard;

import com.aims.backend.domain.commons.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equipment_status")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EquipmentStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status;

}
