package com.aims.backend.mapper;

import com.aims.backend.dto.auth.TokenResponse;

public final class TokenMapper {

    public static TokenResponse.TokenDTO toTokenDTO(
            String accessToken,
            String refreshToken,
            long expiresIn,
            String tokenType
    ) {
        return TokenResponse.TokenDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .tokenType(tokenType)
                .build();
    }
}
