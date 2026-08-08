package com.shop.common.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "shopmax-secret-key-2026-for-jwt-signing-and-verification");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 604800000L);
    }

    @Test
    void generateToken_shouldReturnValidToken() {
        String token = jwtUtil.generateToken(1L);
        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void generateToken_withUserInfo_shouldContainAllClaims() {
        String token = jwtUtil.generateToken(1L, "testuser", "USER");
        assertNotNull(token);

        Long userId = jwtUtil.getUserIdFromToken(token);
        assertEquals(1L, userId);

        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals("testuser", username);

        String userType = jwtUtil.getUserTypeFromToken(token);
        assertEquals("USER", userType);
    }

    @Test
    void generateRefreshToken_shouldReturnRefreshToken() {
        String refreshToken = jwtUtil.generateRefreshToken(1L, "testuser");
        assertNotNull(refreshToken);
        assertTrue(jwtUtil.isRefreshToken(refreshToken));
    }

    @Test
    void isRefreshToken_withAccessToken_shouldReturnFalse() {
        String accessToken = jwtUtil.generateToken(1L);
        assertFalse(jwtUtil.isRefreshToken(accessToken));
    }

    @Test
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtUtil.generateToken(1L);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_withInvalidToken_shouldReturnFalse() {
        assertFalse(jwtUtil.validateToken("invalid-token"));
    }

    @Test
    void validateToken_withNullToken_shouldReturnFalse() {
        assertFalse(jwtUtil.validateToken(null));
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        String token = jwtUtil.generateToken(12345L);
        Long userId = jwtUtil.getUserIdFromToken(token);
        assertEquals(12345L, userId);
    }

    @Test
    void isTokenExpired_withFreshToken_shouldReturnFalse() {
        String token = jwtUtil.generateToken(1L);
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    void getExpirationDateFromToken_shouldReturnFutureDate() {
        String token = jwtUtil.generateToken(1L);
        var expiration = jwtUtil.getExpirationDateFromToken(token);
        assertTrue(expiration.after(new java.util.Date()));
    }
}
