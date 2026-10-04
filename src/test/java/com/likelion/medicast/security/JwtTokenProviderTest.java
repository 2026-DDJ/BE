package com.likelion.medicast.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest {

    private static final String TEST_SECRET =
            "bWVkaWNhc3QtdGVzdC1qd3Qtc2VjcmV0LWtleS0zMi1ieXRlcyE=";

    private final JwtTokenProvider tokenProvider = new JwtTokenProvider(TEST_SECRET, 3_600_000);

    @Test
    void createsAndParsesAccessToken() {
        String token = tokenProvider.createAccessToken(1L);

        assertEquals(1L, tokenProvider.getUserId(token));
        assertEquals(3_600L, tokenProvider.getAccessTokenExpirationSeconds());
    }

    @Test
    void rejectsInvalidToken() {
        assertThrows(JwtException.class, () -> tokenProvider.getUserId("invalid-token"));
    }
}
