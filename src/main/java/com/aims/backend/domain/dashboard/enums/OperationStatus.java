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

    IDLE("대기"),

    STOPPED("정지"),
    
    FAULT("고장"),

    MAINTENANCE("정비");


    /**
     * 화면 표시용 이름
     */
    private final String displayName;
}