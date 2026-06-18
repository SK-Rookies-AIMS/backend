package com.aims.backend.domain.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 자동차 제조 공정 단계
 */
@Getter
@RequiredArgsConstructor
public enum OperationStatus {

    RUNNING("가동"),

    IDLE("대기"),

    STOPPED("정지"),
    
    ERROR("에러"),

    MAINTENANCE("정비");


    /**
     * 화면 표시용 이름
     */
    private final String displayName;
}