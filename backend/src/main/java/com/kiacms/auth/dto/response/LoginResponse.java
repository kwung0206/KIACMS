package com.kiacms.auth.dto.response;

public record LoginResponse(
        String tokenType,
        String accessToken,
        long expiresInSeconds,
        AuthUserResponse user
) {
}
