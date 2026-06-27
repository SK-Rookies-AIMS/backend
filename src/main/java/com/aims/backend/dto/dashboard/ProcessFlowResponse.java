package com.aims.backend.dto.dashboard;

import java.util.List;

public record ProcessFlowResponse(

        List<AgvOperationResponse> agvs

) {
}