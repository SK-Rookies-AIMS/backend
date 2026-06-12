package com.aims.backend.domain.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 자동차 제조 공정 코드
 */
@Getter
@RequiredArgsConstructor
public enum ProcessCode {

    PRESS("프레스"),

    BODY("차체"),

    PAINT("도장"),

    ASSEMBLY("의장"),

    INSPECTION("최종검사");

    /**
     * 화면 표시용 이름
     */
    private final String displayName;
}