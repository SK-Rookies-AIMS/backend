package com.aims.backend.domain.dashboard;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "factory_environment")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FactoryEnvironment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "process_flow", nullable = false)
    private Integer processFlow;

    @Column(name= "usage", nullable = false)
    private Integer usage;

    @Column(name = "temperature", nullable = false)
    private Double temperature;

    @Column(name = "humidity", nullable = false)
    private Double humidity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public FactoryEnvironment(int id, Integer processFlow, Integer usage, Double temperature, Double humidity, LocalDateTime createdAt) {
        this.id = (long) id;
        this.processFlow = processFlow;
        this.usage = usage;
        this.temperature = temperature;
        this.humidity = humidity;
        this.createdAt = createdAt;
    }
}