package com.aims.backend.config.jwt;

public final class JwtConstants {

    public static final String HEADER_STRING = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String ROLE_CLAIM = "role";
    public static final String EMP_NO_CLAIM = "empNo";
    public static final String ID_CLAIM = "id";

    private JwtConstants() {
    }
}
