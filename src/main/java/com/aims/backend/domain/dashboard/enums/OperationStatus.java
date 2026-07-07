package com.aims.backend.domain.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 장비 상태
 */
@Getter
@RequiredArgsConstructor
public enum OperationStatus {

    RUNNING("가동"),


    STOPPED("정지"),
    
    FAULT("고장"),
    
    WARNING("경고");


    /**
     * 화면 표시용 이름
     */
    private final String displayName;
}