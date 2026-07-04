package com.aims.backend.dto.factory_environment;

import com.aims.backend.domain.dashboard.enums.ProcessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class factoryEnvironmentResponse {
    private Long id;
    private ProcessCode processCode;
    private LocalDateTime created_at;
    private double temperature;
    private double humidity;
    private int usage;
}
