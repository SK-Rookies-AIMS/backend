package com.aims.backend.domain.alert;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlertType {
    PROCESS("공정"),
    EQUIPMENT("설비");

    private final String displayName;
}
