package com.aims.backend.domain.dashboard.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Severity {
    CRITICAL("치명적"),
    NORMAL("보통"),
    WARNING("경고");

    private final String displayName;
}