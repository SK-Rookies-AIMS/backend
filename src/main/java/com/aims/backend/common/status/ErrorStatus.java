package com.aims.backend.common.status;

import com.aims.backend.common.code.BaseErrorCode;
import com.aims.backend.common.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {


    RECOMMENDATION_ANALYSIS_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "RECOMMEND404_1",
        "분석 결과를 찾을 수 없습니다."
    ),

    RECOMMENDATION_PRESS_ANALYSIS_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RECOMMEND404_2",
            "PRESS 분석 결과를 찾을 수 없습니다."
    ),

    RECOMMENDATION_SIMILAR_EVENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RECOMMEND404_3",
            "유사 이벤트를 찾을 수 없습니다."
    ),

    RECOMMENDATION_ALERT_EVENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RECOMMEND404_4",
            "유사 이벤트 정보를 찾을 수 없습니다."
    ),

    RECOMMENDATION_TIMELINE_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "RECOMMEND404_5",
            "유사 이벤트의 조치 이력이 존재하지 않습니다."
    ),

    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 내부 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON404", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON405", "지원하지 않는 HTTP 메서드입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER409", "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "사용자를 찾을 수 없습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "USER401", "비밀번호가 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .success(false)
                .code(code)
                .message(message)
                .httpStatus(httpStatus)
                .build();
    }
}
