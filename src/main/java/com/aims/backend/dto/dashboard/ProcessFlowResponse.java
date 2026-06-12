package com.aims.backend.dto.dashboard;

import java.util.List;

/**
 * 공정 흐름도 응답 DTO
 * 현재 화면에 표시할
 * AGV 목록을 반환한다.
 */
public record ProcessFlowResponse(

        List<AgvOperationResponse> agvs

) {
}