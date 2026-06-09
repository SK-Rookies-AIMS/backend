package com.aims.backend.exception.handler;

import com.aims.backend.common.code.BaseErrorCode;
import com.aims.backend.exception.GeneralException;

public class ErrorHandler extends GeneralException {

    public ErrorHandler(BaseErrorCode code) {
        super(code);
    }

    public ErrorHandler(BaseErrorCode code, String message) {
        super(code, message);
    }
}
