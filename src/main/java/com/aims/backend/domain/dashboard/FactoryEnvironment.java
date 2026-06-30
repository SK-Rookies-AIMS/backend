package com.aims.backend.domain.dashboard;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.aims.backend.domain.dashboard.enums.ProcessCode;

@Entity
@Table(name = "factory_environment")
@Getter
@Setter
@NoArgsConstructor
public class FactoryEnvironment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "process_code", nullable = false)
    private ProcessCode processCode;

    @Column(name= "usage", nullable = false)
    private Integer usage;

    @Column(name = "temperature", nullable = false)
    private Double temperature;

    @Column(name = "humidity", nullable = false)
    private Double humidity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public FactoryEnvironment(int id, ProcessCode processCode, Integer usage, Double temperature, Double humidity, LocalDateTime createdAt) {
        this.id = (long) id;
        this.processCode = processCode;
        this.usage = usage;
        this.temperature = temperature;
        this.humidity = humidity;
        this.createdAt = createdAt;
    }
}