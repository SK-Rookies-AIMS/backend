package com.aims.backend.domain.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 자동차 제조 공정 단계
 */
@Getter
@RequiredArgsConstructor
public enum EquipmentType {
    HYDRAULIC_PRESS("유압 프레스"),
    ROBOT_ARM("로봇 팔"),
    CAMERA("카메라"),
    CONVEYOR("컨베이어");

    private final String displayName;
}
