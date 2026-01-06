package com.example.demo.service;

import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);
    RefreshToken rotateRefreshToken(String oldToken, String ipAddress, String userAgent);
    void revokeToken(String token);
    void revokeAllUserTokens(Long userId);
    RefreshToken getRefreshToken(String token);
}
