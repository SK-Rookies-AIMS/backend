package com.aims.backend.dto.dashboard;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgvDispatchTestRequest {

    private String eventId;

    private Long carMasterId;

    private String processCode;

}