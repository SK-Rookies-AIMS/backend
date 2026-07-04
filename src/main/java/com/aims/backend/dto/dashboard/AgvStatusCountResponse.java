package com.aims.backend.dto.dashboard;

import com.aims.backend.domain.dashboard.enums.AgvStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgvStatusCountResponse {
    private AgvStatus agvStatus;
    private Long count;
}
